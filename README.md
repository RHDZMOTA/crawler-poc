# Web Crawler - Proof of Concept

* Current version: *1.0.0*

Crawler POC using **Scala** and **Akka Streams**.

The crawling requests (URLs) are provided by a **Google PubSub** subscription. The crawler downloads and parse 
the HTML to extract the next level urls. Each new URL is publish into the same PubSub topic to repeat the process.
The raw HTML contents are dumped into **Google Storage** and the crawl request information is stored in **Cassandra**.

## Usage

Follow the next steps to configure and run the app:

1. Download this repo: `git clone <remote>`
2. Create the configuration file and add credentials: `cp src/main/resources/application.conf.example src/main/resources/application.conf`
    * You will need to create a service account for [Google Cloud PubSub](https://cloud.google.com/pubsub/docs/access-control) and [Google Cloud Storage](https://cloud.google.com/storage/docs/authentication).
3. Configure Cassandra database.
    * Start the Cassandra service (localhost example): `sudo systemctl start cassandra.service`
    * Open Cassandra Shell: `cqlsh`
    * Create the keyspace (modify in needed): `CREATE KEYSPACE crawler WITH REPLICATION = {'class':'SimpleStrategy', 'replication_factor':1};`
    * Create the url table: `CREATE TABLE crawler.url (id uuid, uri text, depth int, from_url uuid, crawl_request_id uuid, timestamp timestamp, PRIMARY KEY (id, from_url, crawl_request_id));`
4. Run the app: `sbt run`
 
## Contributions

Feel free to add issues or create PRs. Contact the authors for further information.

* [Rodrigo Hernández Mota](https://www.linkedin.com/in/rhdzmota/) 

## License

To be defined.

