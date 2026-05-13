## Wallet - OP - Document Builder

The following DLQ functions are used to isolate the desired metric in Dynatrace:

### API Panels

 - Request Count: cloud.aws.apigateway.countByAccountIdApiIdRegion:filter(eq(apiid,'APIID')):sum 

 - 4xx Error Count: cloud.aws.apigateway."4xxByAccountIdApiIdMethodRegionResourceStage":filter(eq(apiid,'APIID')):sum

 - 5xx Error Count: cloud.aws.apigateway."5xxByAccountIdApiIdMethodRegionResourceStage":filter(eq(apiid,'APIID')):sum

 - Average Latency of Request:  cloud.aws.apigateway.latencyByAccountIdApiIdRegion:filter(eq(apiid,'APIID')):avg