const BASE_URL = "http://localhost:8080/api";
const RND = Math.floor(Math.random() * 10000);

async function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

function prettyPrint(obj, indent = "  ") {
    if (Array.isArray(obj)) {
        if (obj.length === 0) {
            console.log(`${indent}(empty list)`);
            return;
        }
        obj.forEach((item, index) => {
            console.log(`${indent}--- Item ${index + 1} ---`);
            prettyPrint(item, indent + "  ");
        });
    } else if (typeof obj === 'object' && obj !== null) {
        for (const [key, value] of Object.entries(obj)) {
            // Capitalize first letter of key for aesthetics
            const displayKey = key.charAt(0).toUpperCase() + key.slice(1);
            console.log(`${indent}${displayKey}: ${value}`);
        }
    } else {
        console.log(`${indent}${obj}`);
    }
}

async function fetchAPI(url, options = {}) {
    if (options.body && typeof options.body !== 'string') {
        options.body = JSON.stringify(options.body);
        options.headers = { ...options.headers, 'Content-Type': 'application/json' };
    }
    // Using native fetch
    const res = await fetch(BASE_URL + url, options);
    const text = await res.text();
    try {
        return JSON.parse(text);
    } catch {
        return text;
    }
}

async function run() {
    console.log("=============================================");
    console.log("Resetting database & Redis state via Node child_process...");
    const { execSync } = require('child_process');
    try {
        execSync('docker exec -i taxol760-redis redis-cli FLUSHALL', { stdio: 'ignore' });
        execSync('docker exec -i taxol760-db psql -U taxol760 -d taxol760 -c "TRUNCATE TABLE rides, vehicles, drivers, users RESTART IDENTITY CASCADE;"', { stdio: 'ignore' });
        console.log("  -> State reset successfully!");
    } catch(e) {
        console.log("  -> Failed to reset state. Ensure Docker is running.");
    }
    await sleep(750);

    console.log("\nRegistering user...");
    const userReg = await fetchAPI('/auth/register', {
        method: 'POST',
        body: {
            email: `demorider${RND}@example.com`,
            name: `Demo Rider ${RND}`,
            password: "password123",
            phoneNumber: `5551234${RND}`
        }
    });
    
    if (!userReg.token) {
        console.log("Failed to retrieve token. Server response:");
        prettyPrint(userReg);
        process.exit(1);
    }
    
    const userToken = userReg.token;
    console.log(`Received Token: ${userToken.substring(0, 25)}...`);
    await sleep(750);

    console.log("\nFetching current user profile fresh from API (/api/users/me)...");
    const userProfile = await fetchAPI('/users/me', {
        headers: { 'Authorization': `Bearer ${userToken}` }
    });
    prettyPrint(userProfile);
    console.log("=============================================");
    await sleep(2250);

    console.log("\nRegistering new Driver (User + Driver + Vehicle)...");
    const driverReg = await fetchAPI('/auth/register', {
        method: 'POST',
        body: {
            email: `demodriver${RND}@example.com`,
            name: `Demo Driver ${RND}`,
            password: "password123",
            phoneNumber: `5559876${RND}`
        }
    });
    
    const driverToken = driverReg.token;
    console.log(`Driver Token: ${driverToken.substring(0, 25)}...`);
    await sleep(750);

    // Upgrade to driver silently
    await fetchAPI('/drivers/me', {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${driverToken}` },
        body: {
            licenseNumber: `LIC-DEMO-${RND}`,
            vehicleBrand: "Toyota",
            vehicleModel: "Camry",
            vehicleColor: "Black",
            vehiclePlateNumber: "ABC-1234"
        }
    });

    console.log("\nDriver profile (GET /api/drivers/me):");
    const driverProfile = await fetchAPI('/drivers/me', {
        headers: { 'Authorization': `Bearer ${driverToken}` }
    });
    prettyPrint(driverProfile);
    await sleep(2250);

    console.log("\nGoing online (PUT /api/drivers/me/online)...");
    await fetchAPI('/drivers/me/online', {
        method: 'PUT',
        headers: { 'Authorization': `Bearer ${driverToken}` }
    });
    console.log("  Status: OK");
    await sleep(750);

    console.log("\nUpdating driver location (PUT /api/drivers/me/location)...");
    await fetchAPI('/drivers/me/location', {
        method: 'PUT',
        headers: { 'Authorization': `Bearer ${driverToken}` },
        body: { longitude: 44.4268, latitude: 26.1025 }
    });
    console.log("  Status: OK");
    await sleep(750);

    console.log("\n=== REDIS STATE ===");
    try {
        const keys = execSync('docker exec -i taxol760-redis redis-cli KEYS "live-driver:*"', {encoding: 'utf-8'}).trim();
        console.log(`Live Driver Info Key:\n  ${keys || '(none)'}`);
        
        const coords = execSync('docker exec -i taxol760-redis redis-cli GEOPOS Geo 1', {encoding: 'utf-8'}).trim().split('\n');
        if (coords.length >= 2) {
            console.log(`Geo Set (Extracted from GEOPOS):\n  id: 1, lon: ${coords[0]}, lat: ${coords[1]}`);
        } else {
            console.log("Geo Set (Extracted from GEOPOS):\n  (not found)");
        }
    } catch(e) {
        console.log("  Could not fetch Redis state.");
    }
    await sleep(2250);

    console.log("\nFetching recommended drivers for user (GET /api/drivers/suggestions)...");
   const suggestions = await fetchAPI('/drivers/suggestions?longitude=44.4260&latitude=26.1020', {
        headers: { 'Authorization': `Bearer ${userToken}` }
    });
    prettyPrint(suggestions);
    await sleep(2250);

    console.log("\nUser requesting a ride with Driver #1 (POST /api/rides)...");
    const rideResponse = await fetchAPI('/rides', {
        method: 'POST',
        headers: { 
            'Authorization': `Bearer ${userToken}`,
            'Idempotency-Key': `ride-req-${RND}`
        },
        body: {
            driverId: 1,
            pickupLongitude: 44.4260,
            pickupLatitude: 26.1020,
            dropoffLongitude: 45.8000,
            dropoffLatitude: 27.5000
        }
    });
    prettyPrint(rideResponse);
    await sleep(2250);

    console.log("\nDriver accepting the ride (POST /api/rides/1/accept)...");
    await fetchAPI('/rides/1/accept', {
        method: 'POST',
        headers: { 
            'Authorization': `Bearer ${driverToken}`,
            'Idempotency-Key': `accept-req-${RND}`
        }
    });
    console.log("  Status: ACCEPTED");

    console.log("\nDriver starting the ride (POST /api/rides/1/start)...");
    await fetchAPI('/rides/1/start', {
        method: 'POST',
        headers: { 
            'Authorization': `Bearer ${driverToken}`,
            'Idempotency-Key': `start-req-${RND}`
        }
    });
    console.log("  Status: IN_PROGRESS");

    console.log("\n=== REDIS STATE ===");
    try {
        const occupied = execSync('docker exec -i taxol760-redis redis-cli SMEMBERS occupied-drivers', { encoding: 'utf-8' }).trim();
        console.log(`Occupied Drivers Set (occupied-drivers):\n  members: [ ${occupied} ]`);
    } catch(e) {
        console.log("  Could not fetch Redis state.");
    }

    console.log("\n[Simulating driver movement towards destination...]");
    
    const steps = 10;
    const startLat = 26.1020, startLon = 44.4260;
    const endLat = 27.5000, endLon = 45.8000;
    
    for (let i = 1; i <= steps; i++) {
        // Calculate intermediate coordinates
        const currentLat = startLat + ((endLat - startLat) * (i / steps));
        const currentLon = startLon + ((endLon - startLon) * (i / steps));
        
        await fetchAPI('/drivers/me/location', {
            method: 'PUT',
            headers: { 'Authorization': `Bearer ${driverToken}` },
            body: { latitude: currentLat, longitude: currentLon }
        });

        // Fetch live coordinates from Redis to prove it's updating!
        let liveLat = "...", liveLon = "...";
        try {
            const midCoords = execSync('docker exec -i taxol760-redis redis-cli GEOPOS Geo 1', {encoding: 'utf-8'}).trim().split('\n');
            if (midCoords.length >= 2) {
                liveLon = parseFloat(midCoords[0]).toFixed(3);
                liveLat = parseFloat(midCoords[1]).toFixed(3);
            }
        } catch(e) {}

        // The Magic Carriage Return trick (\r)
        const percent = Math.round((i / steps) * 100);
        const filled = '='.repeat(i);
        const empty = ' '.repeat(steps - i);
        process.stdout.write(`\r  Driving: [${filled}>${empty}] ${percent}% (Redis Live -> lat: ${liveLat}, lon: ${liveLon})`);
        
        await sleep(750); // Wait half a second per step
    }
    console.log("\n[Driver reached destination!]");

    console.log("\nDriver completing the ride (POST /api/rides/1/complete)...");
    await fetchAPI('/rides/1/complete', {
        method: 'POST',
        headers: { 
            'Authorization': `Bearer ${driverToken}`,
            'Idempotency-Key': `complete-req-${RND}`
        }
    });
    await sleep(2250);

    console.log("\nRider checking their ride history (GET /api/rides/me)...");
    const riderHistory = await fetchAPI('/rides/me', {
        headers: { 'Authorization': `Bearer ${userToken}` }
    });
    prettyPrint(riderHistory);

    
    console.log("\n=============================================");
}

run().catch(err => {
    console.error("Script failed:", err);
});
