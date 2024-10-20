# Link-COMP4216 

Welcome to the Link-COMP4216 project. This is a group project for COMP4216/5216 In USYD. It is a mobile app designed for the nightlife crowd, providing a seamless way for users to check into venues, discover others in the same location, and build real-time social connections. Whether you're at a bar, club, or event, the app allows you to connect with like-minded people who share the same space.

## Table of Contents

- [Features](#features)
- [Installation Guide](#installation-guide)
- [Basic Usage](#basic-usage)
- [Contribution](#contribution)
- [License](#license)

## Features

- **Registration and Login**: 
  - New users can sign up using their email and password, and existing users can log in using their credentials.
  - Upon registration, users are automatically logged in.

- **Main Page**: 
  - Users can view a list of nearby venues, check in/out of a venue, and see others checked into the same venue.

- **Profile Management**:
  - Users can view and update their profiles by changing their name, username, and profile picture.
  - A logout option is provided to safely exit the app.

- **Matching**:
  - Users can match with others checked into the same venue and send match requests to connect with them.

## Installation Guide

### 1. Install Android Studio and Java Development Kit (JDK)

   - Download and install **[Android Studio](https://developer.android.com/studio)**, ensuring that the latest **JDK** (Java Development Kit) is properly configured.
   - Ensure that the Gradle version in Android Studio matches the one required by the project.

### 2. Clone the Project and Import into Android Studio

   - Clone the project repository:

     ```bash
     git clone https://github.com/Ed-Lai/Link-COMP4216-.git
     ```

   - Alternatively, download the source code as a ZIP file and unzip it.

   - Open **Android Studio**, select **“Open an existing project”**, and navigate to the unzipped project folder.

### 3. Sync Gradle Build

   - Once the project is open, Android Studio will automatically download the necessary dependencies.
   - **Important**: Replace the `google-services.json` file with your own, and configure your **Google Maps API key** and **Firebase credentials**.
   - Firebase setup guides:
     - **[Firebase Database Setup](https://firebase.google.com/docs/database/android/start)**
     - **[Firebase Cloud Storage Setup](https://firebase.google.com/docs/storage/android/start)**
     - **[Add Firebase to Your Android Project](https://firebase.google.com/docs/android/setup)**

### 4. Compile the Project

   - Navigate to **Build > Make Project** in Android Studio to compile the project. Ensure the build is successful.

### 5. Deploy the App to a Device

   - You can deploy the app either to an **Android Studio Emulator** or a **physical device**.
     - **Emulator**: In Android Studio, go to **Run > Run 'app'** and choose the emulator.
     - **Physical Device**: Connect your Android device via USB, enable USB debugging, and deploy the app by selecting your device.

## Basic Usage

### 1. Registration and Login

   - New users can create an account with their email and password, and enter basic information (e.g., name, gender).
   - Existing users can log in using their credentials.

### 2. Main Page: Venue Check-In/Out

   - After logging in, users will see a list of nearby venues.
   - Users can check in to a venue and check out when they leave.

### 3. Profile Management

   - Users can view and edit their profile, including changing their name, username, and profile picture.
   - There is also an option to log out.

### 4. Matching with Others

   - Users can match with other users checked into the same venue and send match requests to connect with them.

## Contribution

We welcome contributions from the community! If you're interested in helping to improve **Link-COMP4216**, please follow these steps:

1. **Fork the repository** on GitHub.

2. **Clone your forked repository** to your local machine:

   ```bash
   git clone https://github.com/your-username/Link-COMP4216-.git
   ```

3. **Create a new branch** for your changes:

   ```bash
   git checkout -b feature/new-feature
   ```

4. **Commit your changes** and push them to your forked repository:

   ```bash
   git add .
   git commit -m "Add new feature"
   git push origin feature/new-feature
   ```

5. Open a **pull request** to have your changes reviewed and merged into the main repository.

Feel free to reach out if you have any questions or need help getting started. We appreciate all contributions, from small fixes to major features!

## License

This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for details.
