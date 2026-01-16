# Budgeting App

A practical Android expense tracking application built with Jetpack Compose and MVVM architecture.
This app helps users manage their monthly budget by tracking expenses, handling recurring costs, and
providing weekly spending insights with a flexible slush fund system.

## Purpose

This app serves two goals:

1. **Practical budgeting tool**: Help manage personal finances with smart weekly budget allocation
2. **Interview preparation**: Demonstrate modern Android development best practices, clean
   architecture, and comprehensive testing

## Core Concept

### Budget Flow

The app uses a **weekly budget system with a flexible slush fund**:

1. **Set Monthly Budget**: User defines their total monthly spending limit
2. **Subtract Recurring Expenses**: Monthly recurring expenses (rent, subscriptions, etc.) are
   automatically deducted
3. **Calculate Weekly Budget**: Remaining amount is divided by ~4.3 weeks (30/7) to get the weekly
   spending allowance
4. **Track Weekly Spending**: Each week (Monday-Sunday), track expenses against the weekly budget
5. **Automatic Slush Fund Management**:
    - **Under budget this week?** → Leftover money automatically adds to slush fund
    - **Over budget this week?** → Overage automatically pulls from slush fund
    - **Manual additions allowed**: Add savings or earmarked money to slush fund anytime
6. **Flexible Expense Sources**: Any expense can be marked to come from slush fund (bypassing weekly
   budget)

### Budget Period

- Monthly budget resets on the 1st of each month
- Weeks run Monday-Sunday

### Example Flow

```
Monthly Budget: $2,000
- Rent: $800 (recurring monthly)
- Netflix: $15 (recurring monthly)
- Phone: $50 (recurring monthly)
= $1,135 available

Weekly Budget: $1,135 ÷ 4.3 weeks = ~$264/week

Week 1: Spent $200 → +$64 to slush fund (now $64)
Week 2: Spent $300 → -$36 from slush fund (now $28)
Week 3: Spent $250 → +$14 to slush fund (now $42)
Week 4: Annual car insurance due ($600)
  → Paid from slush fund first: $600 - $42 = $558 remaining
  → $558 shows as expense for that week
  → Slush fund now at $0
```

## Features

### 1. Recurring Expenses

- Add/edit/delete recurring expenses
- Set frequency: Weekly, Bi-weekly, Monthly, Annually
- Automatically create expense entries when due
- Expenses marked as recurring in the history

### 2. One-Time Expenses

- Quick expense entry with amount, category, and optional notes
- Optional: Mark expense to come from slush fund instead of weekly budget
- Support for 10 predefined categories + custom categories

### 3. Budget Management

- Set monthly budget amount
- View monthly recurring expenses breakdown
- See available amount after recurring expenses
- Calculate and display weekly budget

### 4. Slush Fund

- Automatic accumulation from under-budget weeks
- Automatic deduction from over-budget weeks
- Manual deposits (for savings, earmarked funds, etc.)
- Track balance (can go negative if overspending exceeds fund)

### 5. Annual Expense Reminders

- View upcoming annual expenses as reminders
- Mark as paid when ready
- Payment logic:
    1. Subtract as much as possible from slush fund first
    2. Remainder (if any) shows as regular expense for that week
    3. Expense entry created for tracking

### 6. Dashboard/Home Screen

- Current week's budget
- Current week's spending so far
- Remaining budget for the week
- Amount over budget (if applicable) and slush fund deduction
- Current slush fund balance
- Upcoming annual expense reminders

### 7. Expense Categories

**Predefined Categories:**

- Rent
- Subscription
- Grocery
- Medical
- Necessity
- Entertainment
- Dining
- Travel
- Non-necessity Goods
- Other

**Custom Categories:** Users can add their own categories as needed.

### 8. History & Analysis

- View past weeks and months
- See which weeks went over/under budget
- Category breakdown and spending patterns
- Search and filter expense history

## Architecture

### Tech Stack

- **UI**: Jetpack Compose with Material 3
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Room (SQLite)
- **Dependency Injection**: Hilt (Dagger)
- **Async Operations**: Kotlin Coroutines & Flow
- **Testing**: JUnit, MockK, Turbine (for Flow testing), Compose UI Testing

### Project Structure

```
app/
├── data/
│   ├── local/
│   │   ├── entities/          # Room entity classes
│   │   ├── dao/               # Data Access Objects
│   │   └── BudgetingDatabase  # Room database definition
│   └── repository/            # Repository implementations
│
├── domain/
│   ├── model/                 # Domain models (business objects)
│   └── usecase/               # Business logic use cases
│
├── ui/
│   ├── screens/               # Compose screens
│   ├── components/            # Reusable UI components
│   ├── viewmodel/             # ViewModels for state management
│   └── theme/                 # App theming (colors, typography)
│
├── di/                        # Hilt dependency injection modules
├── util/                      # Utilities and extensions
└── MainActivity               # App entry point
```

### MVVM Architecture Layers

1. **Data Layer**
    - Room entities and DAOs for database operations
    - Repository pattern to abstract data sources
    - Exposes Flows for reactive data streams

2. **Domain Layer**
    - Business logic encapsulated in use cases
    - Domain models separate from database entities
    - Pure Kotlin (no Android dependencies) for easy testing

3. **UI Layer**
    - Jetpack Compose for declarative UI
    - ViewModels manage UI state and handle user interactions
    - StateFlow/Flow for reactive UI updates
    - ViewModels depend only on domain layer (use cases/repositories)

### Key Design Patterns

- **Repository Pattern**: Abstract data sources from business logic
- **Use Case Pattern**: Encapsulate single business operations
- **Observer Pattern**: Flow/StateFlow for reactive data streams
- **Dependency Injection**: Hilt for loose coupling and testability
- **Single Source of Truth**: Room database as the source of truth
- **Unidirectional Data Flow**: Data flows down, events flow up in Compose

## Testing Strategy

### Unit Tests

- **ViewModels**: Test state management and business logic
- **Use Cases**: Test business rules and calculations
- **Repositories**: Test data access and transformation
- **Tools**: JUnit, MockK, Turbine, Truth assertions

### UI Tests

- **Compose UI Tests**: Test user interactions and UI state
- **Navigation Tests**: Verify screen transitions
- **Tools**: Compose Testing framework, Hilt testing utilities

### What We're Testing

- Budget calculations (weekly budget from monthly)
- Slush fund accumulation and deduction logic
- Recurring expense generation
- Annual expense payment logic
- Category filtering and expense queries
- Date-based queries (current week, specific months)
- UI state changes and user interactions

## Development Phases

### Phase 1: Project Setup ✅

- [x] Gradle configuration with all dependencies
- [x] Project structure (data, domain, ui, di layers)
- [x] Basic Compose setup with Material 3 theme
- [x] This README

### Phase 2: Data Layer ✅

- [x] Define data models (Expense, RecurringExpense, Budget, Category)
- [x] Create Room database with entities and DAOs
- [x] Build repository layer
- [x] Unit tests for database operations

### Phase 3: Core Feature - Recurring Expenses (In Progress)

**Sub-phase 3A: Navigation + List Screen** ✅

- [x] Custom blue/teal Material 3 theme
- [x] Compose Navigation setup
- [x] RecurringExpenseListScreen with card-based UI
- [x] Empty state message
- [x] ViewModel with mock data
- [x] Toggle active/inactive functionality

**Sub-phase 3B: Add/Edit Screen** (Next)

- [ ] AddEditRecurringExpenseScreen UI
- [ ] Form inputs (amount, category, frequency, date, description)
- [ ] Reusable components (CategoryPicker, FrequencyPicker, etc.)
- [ ] Navigation integration

**Sub-phase 3C: Wire Up Real Data**

- [ ] Connect ViewModel to repository
- [ ] Implement all CRUD operations
- [ ] Validation logic
- [ ] Error handling

**Sub-phase 3D: Testing**

- [ ] ViewModel unit tests
- [ ] UI tests for both screens

### Phase 4: Core Feature - One-Time Expenses

- [ ] UI to add/edit/delete individual expenses
- [ ] Category selection
- [ ] "From slush fund" option
- [ ] ViewModel + business logic
- [ ] Unit and UI tests

### Phase 5: Budget Setup & Calculation Engine

- [ ] UI to set monthly budget
- [ ] Calculate weekly budget logic
- [ ] Display remaining budget calculation
- [ ] Business logic + comprehensive unit tests

### Phase 6: Slush Fund Management

- [ ] Manual deposit UI
- [ ] Automatic over/under budget calculations
- [ ] Track slush fund balance over time
- [ ] Unit tests for all slush fund operations

### Phase 7: Annual Expense Reminders

- [ ] Display upcoming annual expenses
- [ ] Mark as paid functionality
- [ ] Payment logic (slush fund first, then weekly budget)
- [ ] Create expense entry when paid
- [ ] Unit and UI tests

### Phase 8: Dashboard/Home Screen

- [ ] Current week budget display
- [ ] Current week spending
- [ ] Remaining/over budget calculation
- [ ] Slush fund balance display
- [ ] Upcoming annual expense reminders
- [ ] Navigation to other screens
- [ ] Comprehensive UI tests

### Phase 9: History & Analysis

- [ ] View past weeks/months
- [ ] Week-by-week breakdown
- [ ] Category analysis
- [ ] Search and filtering
- [ ] Charts/visualizations (optional)

## Best Practices Demonstrated

### Code Quality

- **Separation of Concerns**: Clear boundaries between layers
- **Single Responsibility**: Each class has one job
- **Dependency Inversion**: Depend on abstractions (interfaces), not implementations
- **Immutability**: Data classes are immutable where possible
- **Null Safety**: Leveraging Kotlin's null safety features

### Android-Specific

- **Lifecycle Awareness**: ViewModels survive configuration changes
- **Reactive Programming**: Flow/StateFlow for data streams
- **Compose Best Practices**: State hoisting, reusable composables, previews
- **Testing**: Comprehensive unit and UI test coverage
- **Dependency Injection**: Hilt for maintainable, testable code

### Kotlin Features

- **Coroutines**: For async operations without callback hell
- **Flow**: Reactive data streams
- **Data Classes**: Concise model definitions
- **Extension Functions**: Clean, readable utility code
- **Sealed Classes**: Type-safe state management

## Running the App

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 34
- Minimum SDK 26 (Android 8.0)
- Kotlin 1.9.20

### Setup

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Run on emulator or physical device

### Running Tests

```bash
# Unit tests
./gradlew test

# UI tests (requires emulator or device)
./gradlew connectedAndroidTest
```

## Future Enhancements (Post-MVP)

- Export data (CSV, PDF)
- Backup and restore
- Multiple budget profiles (personal, business, etc.)
- Budget goals and savings targets
- Receipt photo attachments
- Widgets for quick expense entry
- Dark mode toggle
- Multi-currency support

## Interview Discussion Points

This project demonstrates:

- ✅ Modern Android architecture (MVVM, Clean Architecture principles)
- ✅ Jetpack Compose for UI
- ✅ Room for local data persistence
- ✅ Hilt for dependency injection
- ✅ Coroutines and Flow for async operations
- ✅ Repository pattern
- ✅ Use case pattern for business logic
- ✅ Comprehensive testing (unit + UI)
- ✅ Separation of concerns
- ✅ Reactive programming with Flow
- ✅ State management in Compose
- ✅ Material 3 design

## License

This is a personal project for learning and interview preparation.

---

**Current Status**: Phase 3A Complete - Navigation and recurring expense list screen implemented
with custom theme and mock data.

**Next Steps**: Continue Phase 3B - Build the add/edit recurring expense screen with form inputs and
validation.
