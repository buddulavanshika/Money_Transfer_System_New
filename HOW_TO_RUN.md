# How to Run the MoneyTransfer Application

This guide explains how to run both the backend (Spring Boot) and frontend (Angular) of the MoneyTransfer application.

## Prerequisites

Before running the application, ensure you have:

1. **Java 17** or higher installed
2. **Maven** installed (or use the Maven wrapper included in the project)
3. **Node.js** v22.12.0 or higher installed (use `nvm install 22.12.0` and `nvm use 22.12.0`)
4. **MySQL** server running on `localhost:3306`
5. **MySQL Database** configured with:
   - Username: `root`
   - Password: `Root123$`
   - The database `moneytransferdb` will be created automatically

## Quick Start (Recommended)

### Option 1: Run in Separate Terminals

#### Terminal 1 - Backend
```bash
cd "c:\Users\labuser\Documents\Training project\MoneyTransfer\backend"
mvn spring-boot:run
```

#### Terminal 2 - Frontend
```bash
cd "c:\Users\labuser\Documents\Training project\MoneyTransfer\frontend"
npm start
```

### Option 2: Run in Background (PowerShell)

```powershell
# Start backend in background
cd "c:\Users\labuser\Documents\Training project\MoneyTransfer\backend"
Start-Process pwsh -ArgumentList "-NoExit", "-Command", "mvn spring-boot:run"

# Start frontend in background
cd "c:\Users\labuser\Documents\Training project\MoneyTransfer\frontend"
Start-Process pwsh -ArgumentList "-NoExit", "-Command", "npm start"
```

## Detailed Setup Instructions

### 1. Database Setup

Ensure MySQL is running and accessible:

```bash
# Test MySQL connection
mysql -u root -p
# Enter password: Root123$
```

The application will automatically:
- Create the database `moneytransferdb` if it doesn't exist
- Run schema migrations
- Seed initial data

### 2. Backend Setup

```bash
# Navigate to backend directory
cd "c:\Users\labuser\Documents\Training project\MoneyTransfer\backend"

# Install dependencies and run
mvn clean install
mvn spring-boot:run
```

**Backend will start on:** `http://localhost:8080`

**Key endpoints:**
- API Base: `http://localhost:8080/api`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health Check: `http://localhost:8080/actuator/health` (if enabled)

### 3. Frontend Setup

```bash
# Navigate to frontend directory
cd "c:\Users\labuser\Documents\Training project\MoneyTransfer\frontend"

# Ensure correct Node version
nvm use 22.12.0

# Install dependencies (if not already done)
npm install

# Start development server
npm start
```

**Frontend will start on:** `http://localhost:4200`

The Angular app will automatically open in your default browser.

## Accessing the Application

1. **Open your browser** and navigate to: `http://localhost:4200`
2. **Login** with the seeded credentials (check `seed-data.sql` for default users)
3. The frontend will communicate with the backend API at `http://localhost:8080/api`

## Default Ports

| Service  | Port | URL |
|----------|------|-----|
| Backend  | 8080 | http://localhost:8080 |
| Frontend | 4200 | http://localhost:4200 |

## Troubleshooting

### Backend Issues

**Problem:** Port 8080 already in use
```bash
# Find and kill process using port 8080
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

**Problem:** Database connection failed
- Verify MySQL is running
- Check credentials in `backend/src/main/resources/application.yml`
- Ensure MySQL is accessible on `localhost:3306`

**Problem:** Maven build fails
```bash
# Clean and rebuild
mvn clean install -U
```

### Frontend Issues

**Problem:** Port 4200 already in use
```bash
# Run on a different port
ng serve --port 4300
```

**Problem:** Module not found errors
```bash
# Reinstall dependencies
rm -rf node_modules package-lock.json
npm install
```

**Problem:** TypeScript compilation errors
```bash
# Clear Angular cache
npm run ng cache clean
npm install
```

## Stopping the Application

### If running in separate terminals:
- Press `Ctrl + C` in each terminal window

### If running in background:
```powershell
# Find and stop Java process (backend)
Get-Process | Where-Object {$_.ProcessName -like "*java*"} | Stop-Process

# Find and stop Node process (frontend)
Get-Process | Where-Object {$_.ProcessName -like "*node*"} | Stop-Process
```

## Development Tips

### Hot Reload

- **Backend:** Spring Boot DevTools is enabled, so changes to Java files will trigger automatic restart
- **Frontend:** Angular dev server watches for changes and auto-reloads the browser

### Building for Production

#### Backend
```bash
cd backend
mvn clean package
java -jar target/backend-0.1.0-SNAPSHOT.jar
```

#### Frontend
```bash
cd frontend
npm run build
# Output will be in dist/frontend/browser
```

## Environment Variables

You can override default configurations using environment variables:

### Backend
```bash
# Custom JWT secret
set JWT_SECRET=your-secret-key-here

# Custom database credentials
set SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/moneytransferdb
set SPRING_DATASOURCE_USERNAME=root
set SPRING_DATASOURCE_PASSWORD=YourPassword
```

### Frontend
Check `frontend/src/environments/` for environment-specific configurations.

## Next Steps

- Review the API documentation at `http://localhost:8080/swagger-ui.html`
- Check the database schema in `backend/src/main/resources/schema.sql`
- Explore the seeded data in `backend/src/main/resources/seed-data.sql`
