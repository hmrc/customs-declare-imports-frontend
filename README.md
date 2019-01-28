# CDS Import Declarations Frontend

[ ![Download](https://api.bintray.com/packages/hmrc/releases/customs-declare-imports-frontend/images/download.svg) ](https://bintray.com/hmrc/releases/customs-declare-imports-frontend/_latestVersion)

This application provides a GOV.UK frontend for the [Customs Declarations API]("http://github.com/hmrc/customs-declarations")
that enables users to submit an import declaration online. The project is currently in the early stages of development.

## Technical Notes

### Dependencies

In order to deliver required functionality, this frontend interacts with a number of other HMRC microservices. These are:

* Declaration submission:
    * [Customs Declarations](https://github.com/hmrc/customs-declarations) - required to process an import declaration submission
    * [Customs Notifications Gateway](https://github.com/hmrc/customs-notification-gateway/) - a transitive dependency from [Customs Declarations](https://github.com/hmrc/customs-declarations)
    * [API Subscription Fields](https://github.com/hmrc/api-subscription-fields) - a transitive dependency from [Customs Declarations](https://github.com/hmrc/customs-declarations)
    which is used by them to verify access permissions for this application to their API 
* Authentication:
    * [Auth](https://github.com/hmrc/auth) - to provide sign in support via Government Gateway
    * [Auth Login API](https://github.com/hmrc/auth-login-api) - transitive dependency required as a backend for the above
    * [Company Auth Frontend](https://github.com/hmrc/company-auth-frontend) - provides the Government Gateway sign in frontend
    * [SSO](https://github.com/hmrc/sso) - provides "single-sign-on" support
    * [User Details](https://github.com/hmrc/user-details) - enables the retrieval of user details
* User Interface:
    * [Assets Frontend](https://github.com/hmrc/assets-frontend) - implementation provider of GOV.UK styles, widgets, and design patterns
* Audit:
    * [Datastream](https://github.com/hmrc/datastream) - sink endpoint for audit logs and messages 
* Testing and running in development:
    * [Auth Login Stub](https://github.com/hmrc/auth-login-stub) - to simulate Government Gateway login
    * [Customs API Hods Stubs](https://github.com/hmrc/customs-api-hods-stubs) - a transitive dependency which is used by [Customs Declarations](https://github.com/hmrc/customs-declarations)
    when running locally to authenticate API requests 

If you are using HMRC's [service manager](https://github.com/hmrc/service-manager), then you can start all of the above
dependencies using [a pre-configured profile](https://github.com/hmrc/service-manager-config):
 * `sm --start CDS_IMPORTS_ALL -f` will start the latest snapshot version of the CDS Import Declarations Frontend and all 
 dependent services, including those required for testing or running in development
 * `sm --start CDS_IMPORTS_DEPS -f` will start all the dependent services; i.e. excluding this frontend itself
 * `sm --start GG_AUTH_SERVICES -f` will start only the dependencies required to satisfy authentication, which is often
 sufficient for the purposes of ongoing development work

When running locally for the purposes of development, you will additionally need to ensure that you have created an API subscription
which grants permission for this application to access the Customs Declarations API. To achieve this, execute the following
CURL command:

```bash
curl -v -X PUT "http://localhost:9650/field/application/customs-declare-imports-frontend/context/customs%2Fdeclarations/version/2.0" -H "Cache-Control: no-cache" -H "Content-Type: application/json" -d '{ "fields" : { "callback-url" : "http://localhost:6789/customs-declare-imports", "token" : "abc59609za2q" } }'
```

### Feature Switching

In order to facilitate continuous integration, A/B testing, and live support, the CDS Import Declarations Frontend supports
[feature toggles](https://martinfowler.com/articles/feature-toggles.html). In the provided implementation, "features" have
one of three potential states:

* `enabled` - the feature is "on".
* `disabled` - the feature is "off".
* `suspended` - the feature is "currently unavailable" (should only be used on a temporary basis for the purposes of live support).

Features can be "switched" on a per-endpoint basis using the [switch action](https://github.com/hmrc/customs-declare-imports-frontend/blob/master/app/controllers/Actions.scala).
Via this mechanism, `enabled` actions will return as expected, those that are `disabled` will return a `404 Not Found` status, and
those that are `suspended` will return `503 Service Unavailable`.

Features can also be "switched" on a more granular basis using conditional checks in code that utilise the feature status 
check support provided in [AppConfig](https://github.com/hmrc/customs-declare-imports-frontend/blob/master/app/config/AppConfig.scala).

In order to determine the status of any given feature, the following locations are checked, in order:

1. System properties
1. The packaged application configuration (i.e. `application.conf`)
1. The `defaultFeatureStatus` (which is set to be `disabled` by default)

Out-of-the-box, all features should be configured to have the default status. They should only be enabled on a per-environment
basis using the mechanisms that are provided to achieve this.

The system or configuration property names for features follow a convention in the form `microservice.services.customs-declare-imports-frontend.features.{featureName}`

When running outside of production or live-like environments, the application provides an endpoint which can be used to
update a feature status. Use `curl -X GET http://$CDS_IMPORTS_HOST/customs-declare-imports/test-only/feature/{featureName}/{status}`.
For example, to update the default feature status to `enabled`, you could do `curl -X GET http://$CDS_IMPORTS_HOST/customs-declare-imports/test-only/feature/default/enabled`.
Alternatively, you could pass the same system property when starting the application; e.g. `sbt "run -Dmicroservice.services.customs-declare-imports-frontend.features.default=enabled"`


###TestEndpoints

in order to enable the test endpoint we need to specify that we use a different router when starting play.
`sbt run -Dapplication.router=testOnlyDoNotUseInAppConf.Routes`
## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
