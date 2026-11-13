package edu.cs4730.restdemo2v2;

import java.net.URI;

public class API {
    public URI Query;
    public URI Update;
    public URI Delete;
    public URI Insert;
    //don't do this for real, but for this example it works.
    static final String kuser = "user1"; // your account name
    static final String kpass = "android"; // your password for the account

    API() {
        try {
            Query =  new URI("http://www.cs.uwyo.edu/~seker/rest/query.php");
            Update = new URI("http://www.cs.uwyo.edu/~seker/rest/update.php");
            Delete = new URI("http://www.cs.uwyo.edu/~seker/rest/delete.php");
            Insert = new URI("http://www.cs.uwyo.edu/~seker/rest/insert.php");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
