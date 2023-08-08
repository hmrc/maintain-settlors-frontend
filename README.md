# Maintain settlors frontend

This service is responsible for updating the information held about a deceased settlor or living settlors in a trust registration.
There is limited information the user is allowed to change for a deceased settlor, as they are the reason the will trust was created.

To run locally using the micro-service provided by the service manager:

### running the service ###

To run locally using the micro-service provided by the service manager:

```
sm2 --start TRUSTS_ALL
```

If you want to run your local copy, then stop the frontend ran by the service manager and run your local code by using the following (port number is 9796 but is defaulted to that in build.sbt).

```
sbt clean run
```

### testing the service ###

To test the service locally run use the following command, this will run both the unit and integration tests and check the coverage of the tests.

```
run_all_tests.sh
```
### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
