# Maintain settlors frontend

This service is responsible for updating the information held about a deceased settlor or living settlors in a trust registration.
There is limited information the user is allowed to change for a deceased settlor, as they are the reason the will trust was created.

To run locally using the micro-service provided by the service manager:

***sm --start TRUSTS_ALL -r***

If you want to run your local copy, then stop the frontend ran by the service manager and run your local code by using the following (port number is 9795 but is defaulted to that in build.sbt).

`sbt run`

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
