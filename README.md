# IRS
A bukkit plugin which taxes players incoming transactions.

This plugin uses the Essentials `UserBalanceUpdateEvent` to tax all transactions
for incoming players. A 8% tax is placed on all incoming transactions (over 10k) 
of users with more than a million dollars. Also, a 6% tax is placed on all 
transactions over a hundred thousand dollars.

# Installation
* You'll need to build this with the `shadowJar` gradle task
  * Windows: `gradlew shawdowJar`
  * macOS/Linux: `./gradlew shadowJar`
* Ensure that you have the [Essentials](https://github.com/EssentialsX/Essentials) 
and [Stonks](https://github.com/schlatt-co/stonks) plugins on your server
* That's all