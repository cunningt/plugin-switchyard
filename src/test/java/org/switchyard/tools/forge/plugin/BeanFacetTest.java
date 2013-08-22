package org.switchyard.tools.forge.plugin;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import org.switchyard.common.io.Files;
import org.switchyard.tools.forge.bean.BeanFacet;
import org.switchyard.tools.forge.bean.BeanServiceConfigurator;
import org.switchyard.tools.forge.bean.BeanReferenceConfigurator;

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
import org.switchyard.tools.forge.plugin.SwitchYardFacet;

@RunWith(Arquillian.class)
public class BeanFacetTest
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
   public void testInstallBeanFacet()
   {
      Assert.assertFalse(project.hasFacet(SwitchYardFacet.class));
      Assert.assertFalse(project.hasFacet(BeanFacet.class));

      SwitchYardFacet switchYard = facetFactory.install(project, SwitchYardFacet.class);
      BeanFacet beanfacet = facetFactory.install(project, BeanFacet.class);
      Assert.assertNotNull(switchYard);
      Assert.assertNotNull(beanfacet);
      System.out.println("facetFactory implemented by" + facetFactory.getClass().getName());
      Assert.assertTrue(project.hasFacet(SwitchYardFacet.class));
      Assert.assertTrue(project.hasFacet(BeanFacet.class));
      Assert.assertNotNull(project.getFacet(SwitchYardFacet.class));
      Assert.assertNotNull(project.getFacet(BeanFacet.class));
      Assert.assertNotNull(switchYard.getMergedSwitchYardConfig().getComposite());
      
   }
   
   @Test
   public void testBeanService() throws IOException
   {
	   SwitchYardFacet switchYard = facetFactory.install(project, SwitchYardFacet.class);
	   BeanFacet beanfacet = facetFactory.install(project, BeanFacet.class);
	   
	   beanServiceConfigurator.newBean(project, "Test");
	   beanReferenceConfigurator.newReference(project, "Test", 
			   "RefName", "com.test.RefIntf");
	   
	   switchYard.saveConfig();	   
	   switchYard.getSwitchYardConfigFile().getFullyQualifiedName();
	   Files.copy(switchYard.getSwitchYardConfigFile().getUnderlyingResourceObject(), 
			   new File("/tmp/switchyard.xml"));
   }
   
   
}
