package xyz.kvantum.addon;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AddOnManagerTest
{

    private static final File TEST_FOLDER = new File( "addons" );
    private static final File ADDON_FOLDER = new File( TEST_FOLDER, "MockAddon" );

    private AddOnManager addOnManager;

    @BeforeAll
    void initAll()
    {
        if ( !Files.exists( TEST_FOLDER.toPath() ) )
        {
            throw new IllegalStateException( "/addons directory does not exist...." );
        }
        this.addOnManager = new AddOnManager( TEST_FOLDER );
    }

    @Test
    void fullTest()
    {
        this.addOnManager.load();
        Assertions.assertEquals( 1, this.addOnManager.getLibraries().size() );
        this.addOnManager.enableAddOns();
        final Optional<AddOn> mockAddon = this.addOnManager.getAddOnInstance( "MockAddon" );
        Assertions.assertNotNull( mockAddon );
        Assertions.assertTrue( mockAddon.isPresent() );
        final AddOn addOn = mockAddon.get();
        Assertions.assertNotNull( addOn );
        Assertions.assertEquals( "MockAddon", addOn.getName() );
        Assertions.assertEquals( addOn, this.addOnManager.getAddOnInstance( addOn.getClass() ).orElse( null ) );
        final AddOn newAddon = this.addOnManager.reloadAddon( addOn );
        Assertions.assertNotNull( newAddon );
        Assertions.assertNotEquals( addOn, newAddon );
        Assertions.assertTrue( newAddon.isEnabled() );
        this.addOnManager.unloadAddon( newAddon );
        Assertions.assertEquals( 0, addOnManager.getAddOns().size() );
    }

    @AfterAll
    void tearDownAll()
    {
        if ( addOnManager != null )
        {
            addOnManager.disableAddons();
        }
        try
        {
            Files.deleteIfExists( ADDON_FOLDER.toPath() );
        } catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

}
