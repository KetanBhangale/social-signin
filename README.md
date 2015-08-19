# social-signin

This is a single android project which is having functionality of login and sharing with Facebook, Twitter and Linkedin at one place.

Pre- requisites:

1. You have setup developer accounts for facebook, twitter and linkedin
2. Make sure you have following things ready with you
  a. For facebook: app key
  b. For twitter: consumer key and consumer secrete
  c. For linkedin: client id, client secrete key, and redirect url.
  
After downloading project structure, import in eclipse/android studio. there are 3 project
1. SignInWithSocial - Our Main project
2. facebook - latest facebook sdk for facebook integration.
3. android-support-v7-appcompat -  version 7 support project

After importing add facebook and android-support-v7-appcompat as library project to our main project SignInWithSocial

Make following changes in the SignInWithSocial application

1. In res --> values --> string.xml --> add your facebook app key
    <string name="fb_app_id">XXXXXXXXXXXXXXXX</string>
2. In AndroidManifest.xml --> add same facebook app key in the authority of facebook content provider.
  <provider android:authorities="com.facebook.app.FacebookContentProviderXXXXXXXXXXXXXXXX" <-- add here
                    android:name="com.facebook.FacebookContentProvider"
                    android:exported="true"/>
                    
3. In com.example.twitter.TwitterLogin activity --> add your consumer_key and secret_key for twitter.
  private static final String consumer_key = "XXXXXXXXXXXXX";
	private static final String secret_key = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
4. In com.example.linkedin.LinkedInLogin activity --> add your account API_KEY and SECRET_KEY
  private static final String API_KEY = "XXXXXXXXX";
	private static final String SECRET_KEY = "XXXXXXXXXXXX";
	
	Also add REDIRECT_URI from your account.
	private static final String REDIRECT_URI = "https://xxx.xxx.com";
	
Currently app the keys used in the app are sample key. You can use these keys to run and check the functionality of the app.

And that's it..... you are good to run the project.

Happy Learning....
