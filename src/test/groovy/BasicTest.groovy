import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement
import org.mule.DefaultMuleMessage
import org.mule.api.MuleMessage
import org.mule.api.client.MuleClient
import org.mule.tck.FunctionalTestCase
//import org.mule.util.IOUtils;

class BasicTest extends FunctionalTestCase {


    @Override
    public void suitePreSetUp() {
        System.out
                .println("******************************** Populate DB **************************");
        String dbURL = "jdbc:derby:memory:cred;create=true";
        Connection conn = null;
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
            // Get a connection
            conn = DriverManager.getConnection(dbURL);
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("CREATE TABLE cred (apikey VARCHAR(45), username VARCHAR(45) NOT NULL ,pw VARCHAR(45))");
            stmt.executeUpdate("INSERT INTO cred VALUES('apikeyid-001', 'albin.kjellin@mulesoft.com','1Mule2demo')");

        } catch (Exception except) {
            except.printStackTrace();
        }

    }


    protected String getConfigResources() {
        // TODO You'll need to edit this file to make the test applicable to
        // your module
        return "mule-config.xml";
    }



    public void testMultiTenancy() throws Exception {

        MuleClient client = muleContext.getClient();
        MuleMessage mm = new DefaultMuleMessage('customersync', ['http.method': 'GET'], muleContext)


        MuleMessage result = client.send("http://localhost:8881/cust001/apikeyid-001/invoice/4028e69635222f9c0135404903a1677b", mm, 100000)
        assertNotNull(result)
        assertNull(result.getExceptionPayload())


        System.out.println("result = " + result.getPayloadAsString())

        String xml = IOUtils.toString(
                getClass().getResourceAsStream("/oagis-invoice-result001.xml"), "UTF-8");
        // TODO Assert the correct data has been received
        assertEquals(xml, result.getPayloadAsString())


    }


    public String prettyPrint(String xml) {
        def stringWriter = new StringWriter()
        def node = new XmlParser().parseText(xml)
        new XmlNodePrinter(new PrintWriter(stringWriter)).print(node)

        return stringWriter.toString()

    }


}