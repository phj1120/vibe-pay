rootProject.name = "vibepay"

include("common-lib:common-core")
include("common-lib:common-security")
include("common-lib:common-messaging")
include("monolith")
include("gateway-service")
include("goods-service")
include("core-service")
include("payment-service")
include("claim-service")
