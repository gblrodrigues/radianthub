# RadiantHub

RadiantHub is an Android application developed in Kotlin with Jetpack Compose, designed to explore agents, abilities, 
maps, and other content from Valorant.

I built this project to practice API integration, local persistence, state management, and modern Android UI 
development while exploring a more immersive visual experience.

- [Features](#features)  
- [Preview](#preview)  
- [Technologies](#technologies-used)
- [Tests](#tests)
- [Technical Decisions](#technical-decisions)   
- [Project Goal](#project-goal)  
- [Contact](#contact)  
- [Disclaimer](#disclaimer)

## Features

| Feature               | Highlights                                                                                                      |
|-----------------------|-----------------------------------------------------------------------------------------------------------------|
| **Home**              | Quick access to Agents, Maps, Cards, Competitive Tiers and Favorites with themed banners                        |
| **Agents**            | Agent browsing with carousel navigation, immersive fullscreen presentation and detailed agent information       |
| **Maps**              | Map browsing with carousel navigation, tactical visualization, map details and interactive callout explorer     |
| **Player Cards**      | Grid-based player card gallery with favorites                                                                   |
| **Competitive Tiers** | Rank browsing organized by divisions with search integration and automatic navigation                           |
| **Favorites**         | Persistent favorites for Agents and Cards, category filtering, undo support and favorite restoration            |
| **Search**            | Global search across Agents, Maps, Cards and Tiers with real-time filtering, navigation and result highlighting |
| **Navigation & UI**   | Drawer, TopBar and BottomBar navigation, theme support, multi-language support and dynamic API localization     |

## Preview
### Home Screen
<img src="https://github.com/user-attachments/assets/a8446100-6236-4d78-a553-858aa50cf815" width="250" alt="HomeScreen"/>

### Agents Screen and Agent Details
<img src="https://github.com/user-attachments/assets/53043637-bff7-4b2c-801b-1f53aea9543e" width="250" alt="AgentsScreen"/>
<img src="https://github.com/user-attachments/assets/adedda93-c83c-433c-8715-faecb35f006e" width="250" alt="AgentDetails"/>

### Maps Screen
<img src="https://github.com/user-attachments/assets/873aa49b-ec7a-4fd1-9bc9-01ad7efd1a2b" width="250" alt="MapsScreen"/>
<img src="https://github.com/user-attachments/assets/4880c099-7ed9-4fb1-87f6-3582c3a9e383" width="250" alt="Callouts"/>

### Favorites Screen
<img src="https://github.com/user-attachments/assets/bbf2ad2c-6e2c-488c-bc5f-8a27479dfb74" width="250" alt="FavoritesScreen"/>
<img src="https://github.com/user-attachments/assets/66c7ea9b-3bbc-43a9-9f0f-fbfdd8cc942b" width="250" alt="Filter Agents"/>
<img src="https://github.com/user-attachments/assets/15479e8f-3a71-40df-972a-a94aa6328047" width="250" alt="Filter Cards"/>

### Search 
<img src="https://github.com/user-attachments/assets/abcf2441-2197-4740-a7ef-96ba2e9c4c00" width="250" alt="Search Chamber Agent"/>
<img src="https://github.com/user-attachments/assets/71835c39-ea68-4b73-bc62-4c646297e8c8" width="250" alt="Redirect to AgentsScreen"/>

Below I will provide a video showing how the application looks:  
> https://github.com/user-attachments/assets/eb3b8d2a-eee6-47c2-97e5-c46cbc20e209

## Technologies used

| Category      | Technology                                                                                                                                                                          | Why                                                                                 |
|---------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------|
| Language      | [![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat&logo=kotlin&logoColor=white)](https://kotlinlang.org/)                                                             | Main development language (my favorite 💜)                                          |
| UI            | [![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=flat&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)           | Declarative UI toolkit for building modern Android interfaces                       |
| Design System | [![Material 3](https://img.shields.io/badge/Material%203-6750A4?style=flat&logo=materialdesign&logoColor=white)](https://m3.material.io/)                                           | Used to maintain visual consistency and modern UI practices                         |
| Networking    | [![Retrofit](https://img.shields.io/badge/Retrofit-3DDC84?style=flat&logo=square&logoColor=white)](https://square.github.io/retrofit/)                                              | HTTP client for REST API consumption                                                |
| DI            | [![Koin](https://img.shields.io/badge/Koin-7F52FF?style=flat&logo=kotlin&logoColor=white)](https://insert-koin.io/)                                                                 | Dependency Injection for simpler and cleaner dependency management                  |
| Persistence   | [![Room](https://img.shields.io/badge/Room-4285F4?style=flat&logo=sqlite&logoColor=white)](https://developer.android.com/training/data-storage/room)                                | Local persistence for favorites, including migration handling and state restoration |
| Preferences   | [![DataStore](https://img.shields.io/badge/DataStore-3DDC84?style=flat&logo=android&logoColor=white)](https://developer.android.com/topic/libraries/architecture/datastore)         | User preference persistence                                                         |
| Testing       | [![JUnit 4](https://img.shields.io/badge/JUnit-4-25A162?style=flat&logo=junit5&logoColor=white)](https://junit.org/)                                                                | Unit testing for search category filtering and empty results                        |
| Image Loading | [![Coil](https://img.shields.io/badge/Coil-FF6F00?style=flat&logo=kotlin&logoColor=white)](https://coil-kt.github.io/coil/compose/)                                                 | Asynchronous image loading via URL                                                  |
| Navigation    | [![Navigation Compose](https://img.shields.io/badge/Navigation%20Compose-4285F4?style=flat&logo=android&logoColor=white)](https://developer.android.com/jetpack/compose/navigation) | Navigation management between screens                                               |

## Tests
Unit tests cover search filtering and ViewModel behavior, including:

* Filtering results across all categories
* Combining data from Agents, Maps, Cards, and Tiers
* Case-insensitive search and queries with no matching results
* Loading and error states
* Preserving the latest query while repositories load

## Technical Decisions

### Architecture (MVVM)
The project follows MVVM with a feature-based organization, keeping UI, domain, and data responsibilities separated.

ViewModels expose screen state through StateFlow, while sealed `UiState` represents loading, success, and error states.

> This structure helps reduce coupling between UI and business logic while keeping screen state predictable.

### API Consumption
The application consumes data from the Valorant public API. This allowed me to practice HTTP requests, data handling, and layer organization within the project.

This allowed me to practice:
* REST API consumption  
* DTO mapping to domain models  
* Error handling  
* UI state management for loading, success, and error scenarios

### Data Persistence (Room + DataStore)
I implemented Room for local persistence of favorite agents and DataStore for managing user personalization preferences such as theme and language selection.

### Screen Navigation (Navigation)
The application uses Navigation Compose with centralized route definitions for parameterized destinations.

Navigation responsibilities are kept separate from screen specific logic, reducing coupling between screens, `ViewModels`, and the `NavigationGraph`.

## Project Goal

This project was developed with the goal of:

* Practicing REST API consumption  
* Improving UI development with Jetpack Compose  
* Improving UI/UX thinking  
* Improving state management using ViewModel and StateFlow  
* Building a stronger Android portfolio project

## Contact
<a href="https://www.linkedin.com/in/gblrodrigues">
  <img src="https://img.shields.io/badge/LinkedIn-100%2B-0077B5?style=flat&logo=linkedin&logoColor=white" alt="My LinkedIn Profile"/>
</a>

## Disclaimer
> [!NOTE]
> This project was developed exclusively for educational and portfolio purposes.
>
> **RadiantHub** is not affiliated with, sponsored by, or endorsed by Riot Games.
>
> The application uses data provided by the **Valorant Community API**, an unofficial community-maintained API and not an official Riot Games API.
>
> **Valorant Community API:** https://valorant-api.com/
>
> VALORANT, Riot Games, and all related properties are trademarks or registered trademarks of Riot Games, Inc.
