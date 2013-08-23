package org.switchyard.tools.forge.plugin;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.switchyard.tools.forge.bean.BeanFacet;
import org.switchyard.tools.forge.bean.BeanReferenceConfigurator;
import org.switchyard.tools.forge.bean.BeanServiceConfigurator;
import org.switchyard.tools.forge.camel.CamelBindingConfigurator;
import org.switchyard.tools.forge.camel.CamelFacet;
import org.switchyard.tools.forge.camel.CamelServiceConfigurator;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;

@RunWith(Arquillian.class)
public class CamelFacetTest
{
   @Deployment
   @Dependencies({
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
            @AddonDependency(name = "org.switchyard.forge:switchyard-forge-plugin", version = "1.0.0-SNAPSHOT"),
            @AddonDependency(name = "org.jboss.forge.addon:projects"),
            @AddonDependency(name = "org.jboss.forge.addon:maven")
   })
   public static ForgeArchive getDeployment()
   {
      return ShrinkWrap.create(ForgeArchive.class).
               addBeansXML().
               addAsAddonDependencies(
                        AddonDependencyEntry.create("org.jboss.forge.addon:projects"),
                        AddonDependencyEntry.create("org.switchyard.forge:switchyard-forge-plugin"),
                        AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
               );
   }

   @Inject
   private ProjectFactory projectFactory;

   @Inject
   private FacetFactory facetFactory;
   
   @Inject
   private SwitchYardConfigurator switchYardConfigurator;
   
   @Inject
   private BeanServiceConfigurator beanServiceConfigurator;
   @Inject
   private BeanReferenceConfigurator beanReferenceConfigurator;
   
   @Inject
   private CamelBindingConfigurator camelBindingConfigurator;
   @Inject
   private CamelServiceConfigurator camelServiceConfigurator;


   private static final String CAMEL_SERVICE = "ForgeCamelService";
   private static final String BEAN_SERVICE = "ForgeBeanService";
   private static final String BEAN_SERVICE_REFERENCEABLE = "ForgeBeanServiceReferenceable";
   private static final String CAMEL_SRV_SUCCESS_MSG = "Created Camel service " + CAMEL_SERVICE;
   private static final String CAMEL_BINDING_SUCCESS_MSG = "Added binding.camel to service " + BEAN_SERVICE;
   private static final String CAMEL_REF_SUCCESS_MSG = "Added binding.camel to reference " + BEAN_SERVICE_REFERENCEABLE;

   
   Project project;

   @Before
   public void setupTestProject()
   {
      project = projectFactory.createTempProject();
      project.getProjectRoot().deleteOnExit();

      MetadataFacet metadataFacet = project.getFacet(MetadataFacet.class);
      metadataFacet.setProjectName("testproject");
      metadataFacet.setProjectVersion("1.0.0-SNAPSHOT");
      metadataFacet.setTopLevelPackage("com.acme.testproject");
   }

   @After
   public void cleanupTestProject()
   {
      if (project != null)
         project.getProjectRoot().delete(true);
   }

   @Test
   public void testInstallSwitchyard() throws Exception
   {
      Assert.assertFalse(project.hasFacet(SwitchYardFacet.class));
      SwitchYardFacet switchYard = facetFactory.install(project, SwitchYardFacet.class);
      CamelFacet camel = facetFactory.install(project, CamelFacet.class);
      BeanFacet bean = facetFactory.install(project, BeanFacet.class);
      
      Assert.assertNotNull(switchYard);
      Assert.assertNotNull(camel);
      Assert.assertNotNull(bean);

      System.out.println("facetFactory implemented by" + facetFactory.getClass().getName());
      Assert.assertTrue(project.hasFacet(SwitchYardFacet.class));
      Assert.assertNotNull(project.getFacet(SwitchYardFacet.class));
      Assert.assertTrue(project.hasFacet(CamelFacet.class));
      Assert.assertNotNull(project.getFacet(CamelFacet.class));
      Assert.assertTrue(project.hasFacet(BeanFacet.class));
      Assert.assertNotNull(project.getFacet(BeanFacet.class));
            
      Assert.assertNotNull(switchYard.getMergedSwitchYardConfig().getComposite());
      
	  beanServiceConfigurator.newBean(project, "Test");
	  beanReferenceConfigurator.newReference(project, "Test", "RefName", "Foo");
	  
	  switchYardConfigurator.promoteService(project, "Test");
	  camelBindingConfigurator.bindService(project, "Test", "'file://target/input?fileName=test.txt'"); 
	  
	  switchYard.saveConfig();
   }
}
 