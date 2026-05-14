## Wallet - OP - Example CRI

The file [ecri-non-prod-dashboard.json](ecri-non-prod-dashboard.json), is a back-up of our Dynatrace non-production dashboard. This file is directly exported from our Dynatrace dashboards, after they have been designed.

Dynatrace uses Dynatrace Query Language (DQL) to express metrics so that we can isolate and manipulate them in graphical form.

The following DQL functions are used to isolate the desired metric in Dynatrace:

### API Panels

 - Request Count: cloud.aws.apigateway.countByAccountIdApiIdRegion:filter(eq(apiid,'APIID')):sum 

 - 4xx Error Count: cloud.aws.apigateway."4xxByAccountIdApiIdMethodRegionResourceStage":filter(eq(apiid,'APIID')):sum

 - 5xx Error Count: cloud.aws.apigateway."5xxByAccountIdApiIdMethodRegionResourceStage":filter(eq(apiid,'APIID')):sum

 - Average Latency of Request:  cloud.aws.apigateway.latencyByAccountIdApiIdRegion:filter(eq(apiid,'APIID')):avg

### How to Export

Dynatrace JSON can be copied from the 'Configure/Dashboard JSON' tab for the dashboard. or the 'Export' tab once inside the dashboard.

Everytime a reviewed and agreed permanent change is made to a dashboard, this file should be updated.

### How to Upload

To upload this dashboard code, you must edit the dashboard in the 'Configure/Dashboard JSON' tab, on the user interface.

You can either copy, or upload the JSON code, to retreive the dashboards.
