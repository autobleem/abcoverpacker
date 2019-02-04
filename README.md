## abcoverpacker
# Small Java CMD line tool to export and update covers

usage: copy jar file into folder that contains coverdb{X}.db files

type:
```
java -jar abcoverpacker.jar -u coversU.db coversU_export 
```
to export all covers into coversU_export folder

edit the covers in graphics tool like gimp or replace covers using same file name (PNG 226x226)

type:
```
java -jar abcoverpacker.jar -p coversU.db coversU_export
```
to put back all the updated files back to database - the database now has all covers reloaded

you can do the same on J or P cover databases.

## NOTE: do not try to import back files between regions .. no covers will be updated
