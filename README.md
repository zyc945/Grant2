# Grant2

inspire by the project [Grant](https://github.com/anthonycr/Grant)

## Gradle usage：

1. add compile dependency

  > compile 'com.zyc945.grant:grant-permissions:0.4.0'

2. add repository

  > repositories{ maven { url '<https://dl.bintray.com/mrz0/maven/>' } }

## Feature：

simplifying android permission request procedure.

## **API List:**

- request permission, under API 23, permission auto callback with granted.

  ```java
  public static synchronized void requestPermissions(Context context, String[] permissions, PermissionsResultAction action)
  ```

- request all permissions defined in the AndroidManifest.xml

  ```java
  public static synchronized void requestAllManifestPermissionsIfNecessary(Activity activity, PermissionsResultAction action)
  ```

- check has permission, return true: had permission

  ```java
  public static synchronized boolean hasPermission(Context context, String permission)
  ```

- check has permissions

  ```java
  public static synchronized boolean hasAllPermissions(Context context, String[] permissions)
  ```

## **Example**

request camera permission.

```java
        PermissionManager.requestPermissions(context, new String[]{Manifest.permission.CAMERA}, new PermissionsResultAction() {
                          @Override
                          public void onGranted() {
                            //permission granted callback
                          }

                          @Override
                          public void onDenied() {
                            //permission denied callback
                          }
          })
```
