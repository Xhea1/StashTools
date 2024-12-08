# StashTools

Tool to tag Stash scenes with data from party.
This works by creating sha256 hashes for the given files and then querying Party with them.
This data is then used to update the corresponding scene in Stash.

## Usage

### Authentication
If your stash instance uses authentication, you need to set the ``STASH_API_KEY`` environment variable to your api key.

Start the app via ``./gradlew run``:

````text
Usage: AddMetadataApp [-hV] [-basePath=<basePath>] -stash=<stashUrl>
                      [-url=<baseUrl>] <input>
Updates Stash scenes with data from Party.
      <input>                The path to a file or directory.
      -basePath=<basePath>   The base Path for files. This will be stripped
                               from the file path when querying Stash.
  -h, --help                 Show this help message and exit.
      -stash=<stashUrl>      The url for the Stash server.
      -url=<baseUrl>         The base URL for the party metadata API. Default
                               is 'https://coomer.su/api/v1'.
  -V, --version              Print version information and exit.
````

## Development

You can use ``./gradlew downloadSchema`` to download a copy of the Stash GraphQL schema to `graphql/stash`.