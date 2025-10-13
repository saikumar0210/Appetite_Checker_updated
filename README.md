# NAICS Demo Application

A full-stack application for NAICS (North American Industry Classification System) company data management with AI-powered insights and recommendations.

## 🚀 Features

- **Company Data Management**: Search, filter, and manage company information
- **NAICS Code Integration**: Automatic NAICS code matching and validation
- **AI-Powered Insights**: Get recommendations for improving confidence scores
- **Interactive Dashboard**: Real-time charts and analytics
- **Responsive Design**: Modern React-based frontend

## 🛠️ Tech Stack

### Backend
- **Java 21** with Spring Boot 3.3.2
- **Spring Data JPA** for data persistence
- **H2 Database** for development
- **Maven** for dependency management

### Frontend
- **React 18** with modern hooks
- **Chart.js** for data visualization
- **CSS3** with responsive design

## 📋 Prerequisites

- Java 21 or higher
- Node.js 16+ and npm
- Maven 3.6+

## 🚀 Quick Start

### 1. Clone the repository
```bash
git clone https://github.com/yourusername/naics-demo.git
cd naics-demo
```

### 2. Backend Setup
```bash
# Build and run Spring Boot application
mvn clean install
mvn spring-boot:run
```
The backend will start on `http://localhost:8080`

### 3. Frontend Setup
```bash
# Navigate to React app directory
cd react-app

# Install dependencies
npm install

# Start development server
npm start
```
The frontend will start on `http://localhost:3000`

## 📁 Project Structure

```
naics-demo/
├── src/main/java/com/example/naicsdemo/    # Spring Boot backend
│   ├── controller/                         # REST controllers
│   ├── service/                           # Business logic
│   ├── repository/                        # Data access layer
│   ├── entity/                           # JPA entities
│   └── dto/                              # Data transfer objects
├── react-app/                            # React frontend
│   ├── src/                              # React components
│   ├── public/                           # Static assets
│   └── package.json                      # Frontend dependencies
├── src/main/resources/                    # Backend resources
│   ├── application.properties            # Spring configuration
│   └── companies.json                    # Sample data
└── pom.xml                               # Maven configuration
```

## 🔧 Configuration

### Backend Configuration
Edit `src/main/resources/application.properties`:
```properties
server.port=8080
spring.h2.console.enabled=true
spring.datasource.url=jdbc:h2:mem:testdb
```

### Frontend Configuration
The React app is configured to proxy API requests to `http://localhost:8080`

## 🚀 Deployment

### Local Development
1. Start backend: `mvn spring-boot:run`
2. Start frontend: `cd react-app && npm start`

### Production Build
```bash
# Build React app
cd react-app
npm run build

# Build Spring Boot jar
mvn clean package
java -jar target/naics-demo-0.0.1-SNAPSHOT.jar
```

## 📊 API Endpoints

- `GET /api/companies` - Get all companies
- `GET /api/companies/search?query={query}` - Search companies
- `POST /api/gemini/recommendation` - Get AI recommendations
- `POST /api/gemini/score-improvement` - Get score improvement tips

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

If you encounter any issues or have questions, please open an issue on GitHub.