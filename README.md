Description:
The Pet Adoption Platform is a Java application which uses an SQL database designed to facilitate the adoption process for animals in need of homes. It serves as a centralized platform where users can search for available animals, submit adoption applications, and communicate with shelters or adoption agencies. The platform also provides a messaging system to streamline communication between users and adoption organizations.

Key Features:
Animal Listings: Search through a variety of animals available for adoption, checking for the desired characteristics.
Adoption Applications: Users can submit adoption applications directly through the platform, by sending messages to the adoption centers for the desired pets.
User Profiles: Users can create profiles to manage their adoption applications, update profile by either changing their name, picture or more important details (such as password or email), and messaging history.
Manager Rights: In order to update the animals for adoption (add or delete), a unique manager code is provided for each organization such that only certain people can add animals via the application.

Database setup:
-> To configure the database for the project, follow these steps <-
Update the db.properties file with your database details.
Utilize importDatabaseEasy to generate the necessary database for the project.
If importDatabaseEasy encounters issues, the repository includes separate files for creating tables and importing data.
