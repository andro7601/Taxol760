const { execSync } = require('child_process');

const BASE_URL = "http://localhost:8080/api";
const NUM_DRIVERS = 20;
const NUM_OCCUPIED = 5;
const CENTER_LAT = 26.1020;
const CENTER_LON = 44.4260;

function sleep(ms) {
    return new Promise(r => setTimeout(r, ms));
}

function randomIp() {
    const r = () => Math.floor(Math.random() * 255);
    return `${r()}.${r()}.${r()}.${r()}`;
}

function randomCoord() {
    const radius = 0.05;
    return {
        lat: parseFloat((CENTER_LAT + (Math.random() * 2 - 1) * radius).toFixed(5)),
        lon: parseFloat((CENTER_LON + (Math.random() * 2 - 1) * radius).toFixed(5))
    };
}

async function fetchAPI(url, options = {}) {
    if (options.body && typeof options.body !== 'string') {
        options.body = JSON.stringify(options.body);
        options.headers = { 'Content-Type': 'application/json', ...options.headers };
    }
    const res = await fetch(BASE_URL + url, options);
    const text = await res.text();
    try { return { status: res.status, data: JSON.parse(text) }; }
    catch { return { status: res.status, data: text }; }
}

async function setupDriver(index) {
    const ip = randomIp();
    const headers = { 'X-Forwarded-For': ip };

    const reg = await fetchAPI('/auth/register', {
        method: 'POST',
        headers,
        body: { email: `sd.driver${index}@example.com`, name: `Driver ${index}`, password: 'pass', phoneNumber: `60000${index}` }
    });
    if (!reg.data.token) return null;

    headers['Authorization'] = `Bearer ${reg.data.token}`;

    await fetchAPI('/drivers/me', {
        method: 'POST',
        headers,
        body: { licenseNumber: `LIC-${index}`, vehicleBrand: 'Toyota', vehicleModel: 'Camry', vehicleColor: 'White', vehiclePlateNumber: `SD-${index}` }
    });

    await fetchAPI('/drivers/me/online', { method: 'PUT', headers });

    const loc = randomCoord();
    await fetchAPI('/drivers/me/location', {
        method: 'PUT',
        headers,
        body: { latitude: loc.lat, longitude: loc.lon }
    });

    const profile = await fetchAPI('/drivers/me', { headers });
    return { index, headers, driverId: profile.data.id };
}

async function run() {
    console.log("=============================================");
    console.log("GEOSEARCH + OCCUPIED-DRIVERS FILTER DEMONSTRATION");
    console.log("=============================================\n");

    console.log("Resetting state...");
    execSync('docker exec -i taxol760-redis redis-cli FLUSHALL', { stdio: 'ignore' });
    execSync(`docker exec -i taxol760-db psql -U taxol760 -d taxol760 -c "TRUNCATE TABLE rides, vehicles, drivers, users RESTART IDENTITY CASCADE;"`, { stdio: 'ignore' });
    console.log("  Done.\n");

    // --- Register 20 drivers concurrently ---
    console.log(`Registering ${NUM_DRIVERS} drivers concurrently, all placed near city center...`);
    const results = await Promise.all(
        Array.from({ length: NUM_DRIVERS }, (_, i) => setupDriver(i + 1))
    );
    const drivers = results.filter(d => d !== null).sort((a, b) => a.driverId - b.driverId);
    console.log(`  ${drivers.length} drivers online and geo-indexed in Redis.\n`);

    // --- Book the 5 geographically closest drivers into active rides ---
    const toOccupy = drivers.slice(0, NUM_OCCUPIED);
    console.log(`Booking the first ${NUM_OCCUPIED} drivers into active rides...`);
    console.log(`  Driver IDs: [ ${toOccupy.map(d => d.driverId).join(', ')} ]`);
    console.log("  (These will be filtered out of suggestions even though they are geographically nearest)\n");

    const activeRides = [];
    for (let i = 0; i < toOccupy.length; i++) {
        const driver = toOccupy[i];
        const ip = randomIp();
        const riderReg = await fetchAPI('/auth/register', {
            method: 'POST',
            headers: { 'X-Forwarded-For': ip },
            body: { email: `sd.rider${i}@example.com`, name: `Rider ${i}`, password: 'pass', phoneNumber: `70000${i}` }
        });
        const riderHeaders = {
            'Authorization': `Bearer ${riderReg.data.token}`,
            'X-Forwarded-For': ip
        };

        const rideRes = await fetchAPI('/rides', {
            method: 'POST',
            headers: { ...riderHeaders, 'Idempotency-Key': `occupy-${i}-${Date.now()}` },
            body: { driverId: driver.driverId, pickupLatitude: CENTER_LAT, pickupLongitude: CENTER_LON, dropoffLatitude: 28.0, dropoffLongitude: 46.0 }
        });

        if (rideRes.data.id) {
            await fetchAPI(`/rides/${rideRes.data.id}/accept`, {
                method: 'POST',
                headers: { ...driver.headers, 'Idempotency-Key': `accept-${i}-${Date.now()}` }
            });
            await fetchAPI(`/rides/${rideRes.data.id}/start`, {
                method: 'POST',
                headers: { ...driver.headers, 'Idempotency-Key': `start-${i}-${Date.now()}` }
            });
            activeRides.push({ rideId: rideRes.data.id, driver });
        }
    }
    console.log(`  ${activeRides.length} rides now IN_PROGRESS.\n`);

    // --- Show Redis occupied-drivers set ---
    console.log("=== REDIS STATE ===");
    const occupiedBefore = execSync('docker exec -i taxol760-redis redis-cli SMEMBERS occupied-drivers', { encoding: 'utf-8' }).trim();
    console.log(`occupied-drivers Set:\n  members: [ ${occupiedBefore.split('\n').join(', ')} ]`);
    console.log("==================\n");
    await sleep(2500);

    // --- Register query rider and query suggestions ---
    const queryRider = await fetchAPI('/auth/register', {
        method: 'POST',
        body: { email: 'sd.queryrider@example.com', name: 'Query Rider', password: 'pass', phoneNumber: '8888888' }
    });
    const queryHeaders = { 'Authorization': `Bearer ${queryRider.data.token}` };

    console.log("Rider at city center queries for available drivers (GET /api/drivers/suggestions)...");
    const suggestions1 = await fetchAPI(`/drivers/suggestions?latitude=${CENTER_LAT}&longitude=${CENTER_LON}`, {
        headers: queryHeaders
    });

    console.log("  Suggestions returned:");
    suggestions1.data.forEach((d, i) => {
        console.log(`    ${i + 1}. Driver ID: ${d.id} | Plate: ${d.vehiclePlateNumber}`);
    });

    const occupiedIds = toOccupy.map(d => d.driverId);
    const leaked = suggestions1.data.filter(d => occupiedIds.includes(d.id));
    if (leaked.length === 0) {
        console.log(`\n  Occupied drivers [ ${occupiedIds.join(', ')} ] do not appear in results.`);
        console.log("  GEOSEARCH output is correctly filtered against the occupied-drivers Redis Set.");
    } else {
        console.log(`\n  WARNING: ${leaked.length} occupied driver(s) leaked into suggestions.`);
    }
    await sleep(2500);

    // --- Complete all active rides ---
    console.log("\nCompleting all active rides...");
    for (const { rideId, driver } of activeRides) {
        await fetchAPI(`/rides/${rideId}/complete`, {
            method: 'POST',
            headers: driver.headers
        });
    }
    console.log("  All rides completed.\n");

    // --- Show Redis occupied-drivers set again (should be empty) ---
    console.log("=== REDIS STATE ===");
    const occupiedAfter = execSync('docker exec -i taxol760-redis redis-cli SMEMBERS occupied-drivers', { encoding: 'utf-8' }).trim();
    console.log(`occupied-drivers Set:\n  members: [ ${occupiedAfter || 'empty'} ]`);
    console.log("==================\n");
    await sleep(2500);

    // --- Query suggestions again ---
    console.log("Rider queries suggestions again after all rides completed...");
    const suggestions2 = await fetchAPI(`/drivers/suggestions?latitude=${CENTER_LAT}&longitude=${CENTER_LON}`, {
        headers: queryHeaders
    });

    console.log("  Suggestions returned:");
    suggestions2.data.forEach((d, i) => {
        console.log(`    ${i + 1}. Driver ID: ${d.id} | Plate: ${d.vehiclePlateNumber}`);
    });

    const reappeared = suggestions2.data.filter(d => occupiedIds.includes(d.id));
    if (reappeared.length > 0) {
        console.log(`\n  Previously occupied drivers [ ${reappeared.map(d => d.id).join(', ')} ] are back in suggestions.`);
    }

    console.log("\n=============================================");
}

run().catch(err => {
    console.error("Script failed:", err);
});
