# Ranplate
A template repo for my minecraft mods which is multi-loader & multi-versioned

Current Preprocessor System:
- MC_`<version>`
   - so `MC_1_19_2` would mean if 1.19.2 is the current version
- POST_MC_`<version>`
   - so `POST_MC_1_19_2` would mean if the version is after 1.19.2
- POST_CURRENT_MC_`<version>`
   - so `POST_CURRENT_MC_1_19_2` would mean if the current version is after 1.19.2 or is 1.19.2
- PRE_MC_`<version>`
   - so `PRE_MC_1_19_2` would mean if the version is before 1.19.2
- PRE_CURRENT_MC_`<version>`
   - so `PRE_CURRENT_MC_1_19_2` would mean if the current version is before 1.19.2 or is 1.19.2

Remember to install the [Manifold](https://plugins.jetbrains.com/plugin/10057-manifold) plugin in IntelliJ!\
Also could look in this [class](https://github.com/Ran-helo/Ranplate/blob/master/common/src/main/java/net/examplemod/ExampleMod.java) if you need an example of how to use it!

This repo contains [MixinExtras](https://github.com/LlamaLad7/MixinExtras) so remember to [check](https://github.com/LlamaLad7/MixinExtras/wiki) that out!\
It also contains a lot of optimization mods to run the development environment a bit better.\
And also contains [Forgix](https://github.com/PacifistMC/Forgix) which basically merges the mod-loaders into one jar!

---
When adding more versions, remember to edit the `.github/workflows/build.yml` file and make sure the merged jars doesn't overwrite each other!\
And do note that if you're going to use Architectury API then `dev.architectury` is `me.shedaniel.architectury` in 1.16.5 and below.
