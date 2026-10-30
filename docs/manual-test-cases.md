# Manual test cases

The API test case this framework automates, written as a tester would run it by hand (curl, Postman
or a browser). The TestNG method that covers it starts with its ID
(`tc201_validateEmailFormatOnComments`) and so does its description (`[TC-201] ...`), so a failure
in the report leads straight to the case below.

## Setup

- Base URL: <https://jsonplaceholder.typicode.com> (a public fake API).
- Test user: username `Samantha` (`src/test/resources/data.properties`).
- Valid e-mail: `^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,6}$`, case-insensitive, e.g. `name@example.com`.

## Summary

| ID | Title | Priority | Automated by |
|---|---|---|---|
| TC-201 | Every e-mail in the comments on the user's posts is valid | High | `CommentTest.tc201_validateEmailFormatOnComments` |

---

### TC-201 Every e-mail in the comments on the user's posts is valid

| # | Step | Expected result |
|---|---|---|
| 1 | `GET /users?username=Samantha` | `200`; one user; note the `id` (e.g. `3`) |
| 2 | `GET /posts?userId=<id>` | `200`; the user's posts |
| 3 | For each post `GET /comments?postId=<post id>` | `200` each; the comments |
| 4 | Check every comment's `email` against the pattern above | All valid. Note **every** invalid address with its comment id before failing the case |
