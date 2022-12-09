## Development

* I developed the project using IntelliJ IDEA Ultimate Edition.
* As Java 14 is installed on my computer, I set the version to 14 in the build.gradle file.
* For getters-setters and constructors, I preferred using Lombok. Classes look much more clear in that way.
  * You can see Lombok-related configurations added into the build.gradle file too.
* Apart from JavaDocs, I wrote some small comments in the codes for the sake of clarification too.
* I paid attention to warnings and tips both of the SonarLint plugin and the IDE itself.
* Before the submission, I automatically rearranged/optimized imports and automatically reformatted (beautified) codes in all classes.

## The new `amount` column in the `payment` table

I took the initiative and decided to add `amount` column to the `payment` table. In my opinion, each transaction record in this table should show the amount of the transferred money too.


## Testing: Account creation

The `account` table is created by `schema.sql`, however there is no initial account data. To test payment, some account data must be created first.

* You can insert account data manually into the `account` table.
* The automatically executed `schema.sql` may be modified. INSERT queries to insert account data can be put into the file. Each Spring application run can execute account insertion code too.
* Above all, I decided to create a dedicated controller `AccountController` that holds the endpoint `account` which can be called to create accounts in the system.
  * **Postman** can be used to send a POST request to the following URL to call the aforementioned endpoint: 
    `http://localhost:8080/account/`
  * The content type is `application/json` and the body format is raw JSON.
  * The endpoint's request body accepts a list of accounts to be created, which means you can send information of multiple accounts to be created within the same JSON at once.
    * An example request JSON for this endpoint, which creates two accounts:
        ```
        {
          "accounts": [
            {
              "name": "account one",
              "balance": "100.00"
            },
            {
              "name": "account two",
              "balance": "200.00"
            }
          ]
        }
        ```
    * All balance inputs in this request body must be non-negative as per the related requirement. In case of at least one account JSON object with negative balance value, the application throws the custom exception with the relevant code and message.
