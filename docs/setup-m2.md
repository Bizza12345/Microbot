# Pre-populated Maven repository

To build Microbot without network access you can download a prebuilt `.m2` folder.
Install the prerequisites if they are missing:

```bash
sudo apt-get install -y curl unzip
pip install --user gdown
```

Then run the helper script from the project root:

```bash
./scripts/setup-m2.sh
```

The archive is fetched from Google Drive and unpacked so that the cache resides in `~/.m2/repository`.
