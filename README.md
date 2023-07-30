# SourceModLoader

Download and install mods from source!

_Requires loader plugins to be enabled._

---

#### Example `config/sourcemodloader/mods.json`:

```json
{
  "sources": [
    {
      "name": "global-options",
      "type": "Archive",
      "url": "https://github.com/Abbie5/global-options/archive/refs/tags/v1.0.tar.gz",
      "sha256": "71bbd4c212769eb5f14e0ce429c57d238608f876574c19e35f11264c1b3b5de7",
      "buildsystem": {
        "type": "Gradle"
      },
      "artifact": "build/libs/global_options-1.0.jar"
    },
    {
      "name": "botania",
      "type": "Git",
      "url": "https://github.com/VazkiiMods/Botania.git",
      "ref": "1.20.x",
      "buildsystem": {
        "type": "Gradle"
      },
      "artifact": "Fabric/build/libs/Botania-1.20.1-441-FABRIC-SNAPSHOT.jar"
    }
  ]
}
```
