# CONS3RT Plugin

The CONS3RT jenkins plugin provides users with the ability to create CONS3RT software assets as part of the jenkins build. The plugin also allows for users to upload their asset, created as a function of their build, to a CONS3RT site. This allows for the creation of new assets or the update of existing assets as part of a CI pipeline. In addition to asset creation or update, users can also leverage the deployment run option in the post-build action section of the plugin to launch new deployment runs that leverage their newly updated asset(s). The CONS3RT plugin allows for the full customization and configuration of deployment runs.

# Post-Build Step:
It is here that a user can define an asset for creation. The asset to be created must have a name defined (this name will be reflected in the CONS3RT site following upload). In addition, a user can specify a description and versioning information about their asset. The resource requirements of an asset can also be defined, denoting what things are required on the underlying system for the asset to run, including platform, architecture, bits, cpu, ram, and finally storage. Each of these fields can either be set or omitted and will serve to determine what systems the asset can run on once it has been imported into the CONS3RT site. Lastly, the contents of the asset are defined.

# Post-Build Action:
In order to use this portion of the CONS3RT, a site configiration must first be added. This site configuration defines a user account and CONS3RT site to connect to. A site url and ReST Token must be provided. The ReST Token must taken the form of a "secret text" jenkins credential. Based on the site being connected to, either a username or certificate must be provided. A certificate must be provided as a "certificate" jenkins credential where as a username can be provided as plain test. Once all of the above items have been provided, connection to the site can be tested via the "Check Connection" button.
