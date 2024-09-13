# gn_mobile_maps

Android Map library based on [osmdroid](http://osmdroid.github.io/osmdroid/index.html).
* `maps`: The library itself
* `app`: Demo app

See [settings documentation](/maps).

## Upgrade git sub modules

Do **NOT** modify directly any git sub modules (e.g. `mountpoint`).
Any changes should be made from each underlying git repository:

* `mountpoint`: [gn_mobile_core](https://github.com/PnX-SI/gn_mobile_core) git repository

```bash
./upgrade_submodules.sh
```
