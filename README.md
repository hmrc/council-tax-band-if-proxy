
# council-tax-band-if-proxy

CCYCTB Integration Framework proxy

Proxy service to forward requests from CCYCTB frontend to Integration Framework https://ifs.ws.ibt.hmrc.gov.uk

`council-tax-band-if-proxy` service must be deployed to `protected` zone to have direct connection to Integration Framework server.

## Run the service

Run council-tax-band-if-proxy locally on default port 8882

> sbt run

URL samples:

- http://localhost:8882/valuations/get-properties/Search?postCodeStandardSearch=M11%201AE
- http://localhost:8882/valuations/get-property/121c87c6-1040-4f93-bdf8-2c6d67f1b5cb

### Service manager

Run council-tax-band-if-proxy by SM

> sm2 --start COUNCIL_TAX_BAND_IF_PROXY COUNCIL_TAX_BAND_STUBS


or run all CCYCTB services

> sm2 --start VOA_CTI_ACCEPTANCE


### License

This code is open source software licensed under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html).
