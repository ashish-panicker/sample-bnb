# Airbnb Clone: Catalog & Pricing Microservices

This architectural module handles the property lifecycle and the dynamic calculation of stay costs. The two services are decoupled via **Apache Kafka** to ensure high availability and performance.

## 1. Catalog Service (The Source of Truth)

**Port:** `9081`

**Database:** H2 (In-Memory)

**Purpose:** Manages the static details of a property (title, location, type). It acts as the **Producer** of events.

### API Endpoints

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/v1/listings` | Create a new property listing. |
| `GET` | `/api/v1/listings` | Retrieve all active listings. |
| `GET` | `/api/v1/listings/{id}` | Get specific listing details. |

---

## 2. Pricing Service (The Logic Engine)

**Port:** `9082`

**Database:** H2 (In-Memory)

**Purpose:** Manages dynamic pricing rules (weekend hikes, cleaning fees) based on property types. It acts as the **Consumer** of events.

### API Endpoints

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/api/v1/pricing/quote/{id}` | Get a full price breakdown for a listing. |
| `PUT` | `/api/v1/pricing/rules/{id}` | Update specific pricing multipliers for a listing. |

---

## 3. Availbility Service (The Logic Engine)

**Port:** `9083`

**Database:** H2 (In-Memory) + Redis

**Purpose:** Manages the booking calendar and prevents double-bookings using Redis distributed locks..

### API Endpoints

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/api/v1/availability/{id}` | Check available dates for a specific listing. |
| `POST` | `/api/v1/availability/lock` |Place a 10-minute "Soft Lock" on dates in Redis. |

---

## Event-Driven Workflow 

Instead of services polling each other, we use a **reactive push model** via Kafka. When a host interacts with the Catalog, the downstream services synchronize their local state automatically.

### Step-by-Step Logic:

1. **Creation:** A host creates a listing via the **Catalog Service** (e.g., a "Villa" in Santorini).
2. **Persistence:** The Catalog Service saves the record to its local H2 database.
3. **Emission:** The Catalog Service publishes a `ListingEvent` to the Kafka topic `listing-events`.
* *Payload:* `{ "listingId": 104, "basePrice": 1200.0, "propertyType": "Villa", "status": "CREATED" }`.


4. **Parallel Ingestion:** Both the **Pricing** and **Availability** services are subscribed to the `listing-events` topic and receive the message simultaneously.
5. **Smart Processing (Pricing Service):**
* **Consumption:** The service picks up the "Villa" type.
* **Logic:** Using a **Java Switch Expression**, it maps the "Villa" type to luxury-tier multipliers (e.g., 1.5x weekend hike).
* **Initialization:** It saves these rules to its local database. Future price quotes for ID 104 are now handled entirely within the Pricing Service.


6. **Calendar Setup (Availability Service):**
* **Consumption:** The service identifies that a new inventory item (ID 104) is live.
* **Logic:** It initializes the availability state. This involves creating a persistent record in its DB and "pre-warming" a **Redis Bitset** where the next 365 bits are set to `0` (Available).
* **Initialization:** The calendar is now "open" for business. Any subsequent "soft locks" or booking requests for ID 104 will be validated against this local Redis/DB state.

---

## Summary of Service Independence

| Service | Data it Stores Locally | Decision Logic |
| --- | --- | --- |
| **Catalog** | Title, Desc, Location, HostID | Is the listing active/deleted? |
| **Pricing** | `listingId`, Multipliers, Fees | What is the total cost for these dates? |
| **Availability** | `listingId`, Redis Bitset, Bookings | Are these dates free to be locked? |

---

### Why this scales:

If we later add a **Search Service** or an **Image Processing Service**, we simply point them to the same Kafka topic. The **Catalog Service** code never has to change to accommodate new features.

**Would you like me to provide the Java code for the `AvailabilityController` and the Redis "Soft Lock" logic next?**

---

## Tech Stack

* **Java 17+** (using Switch Expressions & Records)
* **Spring Boot 3.x**
* **Apache Kafka** (Message Broker)
* **Spring Data JPA** (Persistence)
* **H2** (Relational Database)

---

### Key Advantage

By using this workflow, the **Pricing Service** can function even if the **Catalog Service** is offline. It has its own copy of the necessary data, which significantly reduces system latency and improves the user experience during the booking process.
