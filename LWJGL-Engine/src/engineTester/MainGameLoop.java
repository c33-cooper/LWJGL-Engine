package engineTester;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.xml.crypto.Data;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import entities.Camera;
import entities.Entity;
import entities.Light;
import entities.Player;
import guis.GuiRenderer;
import guis.GuiTexture;
import models.RawModel;
import models.TexturedModel;
import objConverter.ModelData;
import objConverter.OBJFileLoader;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import renderEngine.EntityRenderer;
import shaders.StaticShader;
import terrains.Terrain;
import textures.ModelTexture;
import textures.TerrainTexture;
import textures.TerrainTexturePack;

public class MainGameLoop {

	public static void main(String[] args) {

		DisplayManager.createDisplay();
		Loader loader = new Loader();
		
		// Obj File Loaders to convert
		ModelData treeData = OBJFileLoader.loadOBJ("tree");
		ModelData grassData = OBJFileLoader.loadOBJ("grassModel");
		ModelData fernData = OBJFileLoader.loadOBJ("fern");
		ModelData flowerData = OBJFileLoader.loadOBJ("grassModel");
		ModelData playerData = OBJFileLoader.loadOBJ("person");
		ModelData lowPolyTreeData = OBJFileLoader.loadOBJ("lowPolyTree");
		ModelData boxData = OBJFileLoader.loadOBJ("box");
		ModelData lampData = OBJFileLoader.loadOBJ("lamp");
		
		// Static Models
		RawModel treeStaticModel = loader.loadToVAO(treeData.getVertices(), treeData.getTextureCoords(),
				treeData.getNormals(), treeData.getIndices());
		RawModel grassStaticModel = loader.loadToVAO(grassData.getVertices(), grassData.getTextureCoords(),
				grassData.getNormals(), grassData.getIndices());
		RawModel fernStaticModel = loader.loadToVAO(fernData.getVertices(), fernData.getTextureCoords(),
				fernData.getNormals(), fernData.getIndices());
		RawModel flowerStaticModel = loader.loadToVAO(flowerData.getVertices(), flowerData.getTextureCoords(),
				flowerData.getNormals(), flowerData.getIndices());
		RawModel playerStaticModel = loader.loadToVAO(playerData.getVertices(), playerData.getTextureCoords(),
				playerData.getNormals(), playerData.getIndices());
		RawModel lowPolyTreeModel = loader.loadToVAO(lowPolyTreeData.getVertices(), lowPolyTreeData.getTextureCoords(),
				lowPolyTreeData.getNormals(), lowPolyTreeData.getIndices());
		RawModel boxModel = loader.loadToVAO(boxData.getVertices(), boxData.getTextureCoords(),
				boxData.getNormals(), boxData.getIndices());
		RawModel lampModel = loader.loadToVAO(lampData.getVertices(), lampData.getTextureCoords(),
				lampData.getNormals(), lampData.getIndices());
		
		// Texture Atalases
		ModelTexture fernTextureAtlas = new ModelTexture(loader.loadTexture("fernAtlas"));
		fernTextureAtlas.setNumberOfRows(2);
		
		// Textured Models
		TexturedModel treeModel = new TexturedModel(treeStaticModel, new ModelTexture
				(loader.loadTexture("tree")));
		TexturedModel grassModel = new TexturedModel(grassStaticModel, new ModelTexture
				(loader.loadTexture("grassTexture")));
		TexturedModel fernModel = new TexturedModel(fernStaticModel, fernTextureAtlas);
		TexturedModel flowerModel = new TexturedModel(flowerStaticModel, new ModelTexture
				(loader.loadTexture("flower")));
		TexturedModel personModel = new TexturedModel(playerStaticModel, new ModelTexture
				(loader.loadTexture("playerTexture")));
		TexturedModel lowPolyTreeTexModel = new TexturedModel(lowPolyTreeModel, new ModelTexture
				(loader.loadTexture("tree")));
		TexturedModel boxTexModel = new TexturedModel(boxModel, new ModelTexture
				(loader.loadTexture("box")));
		TexturedModel lampTexModel = new TexturedModel(lampModel, new ModelTexture
				(loader.loadTexture("lamp")));
		
		// Set Transparent textured models here
		grassModel.getTexture().setHasTransparency(true);
		fernModel.getTexture().setHasTransparency(true);
		flowerModel.getTexture().setHasTransparency(true);
		
		// Set textures to use fake lighting so that normals will
		// all point upwards, this is useful for textures with multiple 
		// faces pointing in different directions i.e a grass texture.
		grassModel.getTexture().setUseFakeLighting(true);
		fernModel.getTexture().setUseFakeLighting(true);
		flowerModel.getTexture().setUseFakeLighting(true);
		
		//Entity entity = new Entity(staticModel, new Vector3f(0, 0, -50), 0, 0, 0, 1);
		// A list of all entities to be rendered in one draw call per frame.
		List<Entity> entities = new ArrayList<Entity>();
		Random random = new Random();
		
		// Terrain Texture stuff
		TerrainTexture backgroundTexture = new TerrainTexture(loader.loadTexture("grassy2"));
		TerrainTexture rTexture = new TerrainTexture(loader.loadTexture("mud"));
		TerrainTexture gTexture = new TerrainTexture(loader.loadTexture("grassFlowers"));
		TerrainTexture bTexture = new TerrainTexture(loader.loadTexture("path"));
		TerrainTexture blendMap = new TerrainTexture(loader.loadTexture("blendMap"));
		
		TerrainTexturePack texturePack = new TerrainTexturePack(backgroundTexture, rTexture, gTexture, bTexture);
		
		// Multiple terrain objects
		Terrain terrain = new Terrain(0, -1, loader, texturePack, blendMap, "heightmap");
		Terrain terrain2 = new Terrain(-1, -1, loader, texturePack, blendMap, "heightmap");
		
		// Create lights list and add sun to list.
		List<Light> lights = new ArrayList<Light>();
		Light sun = new Light(new Vector3f(0, 1000, -7000), new Vector3f(0.4f, 0.4f, 0.4f));
		lights.add(sun);
		
		// Player object
		Player player = new Player(personModel, new Vector3f(100, 0, -50), 0, -198.87997f, 0, 1);
		Camera camera = new Camera(player);
		
		// GUI objects
		List<GuiTexture> guis = new ArrayList<GuiTexture>();
		GuiTexture gui = new GuiTexture(loader.loadTexture("minimap"), new Vector2f(0.8f, 0.7f), new Vector2f(0.25f, 0.25f));
		guis.add(gui);
		// Loop through multiple attenuated light + lamp sources
		for (int i = 0; i < 15; i++) {
			// X, Y, Z positions
			float x = random.nextFloat()*800 - 400;
			float z = random.nextFloat() * -800;
			float y = terrain.getHeightOfTerrain(x, z);
			// Random RGB colours
			float r = 5.0f - random.nextFloat();
			float g = 5.0f - random.nextFloat();
			float b = 5.0f - random.nextFloat();
			
			// Lamps which lights will attenuate from
			entities.add(new Entity(lampTexModel, new Vector3f(x, y, z), 0, 0, 0, 1));
			
			// Create multiple light sources and add to list
			lights.add(new Light(new Vector3f(x, (y) + (10), z), new Vector3f(r, g, b), new Vector3f(1, 0.01f, 0.002f)));
		}
		
		// Loop through multiple trees and allocate random positions
		for (int i = 0; i < 500; i++) {
			// X, Y, Z positions
			float x = random.nextFloat()*800 - 400;
			float z = random.nextFloat() * -800;
			float y = terrain.getHeightOfTerrain(x, z);
			entities.add(new Entity(treeModel, new Vector3f(x, y, z),0,0,0,5));
		}
		for (int i = 0; i < 200; i++) {
			// X, Y, Z positions
			float x = random.nextFloat()*800 - 400;
			float z = random.nextFloat() * -800;
			float y = terrain.getHeightOfTerrain(x, z);
			entities.add(new Entity(lowPolyTreeTexModel, new Vector3f(x, y, z),0,0,0,1));
		}
		for (int i = 0; i < 80; i++) {
			// X, Y, Z positions
			float x = random.nextFloat()*800 - 400;
			float z = random.nextFloat() * -800;
			float y = terrain.getHeightOfTerrain(x, z);
			entities.add(new Entity(grassModel, new Vector3f(x, y, z),0,0,0,2));
		}
		for (int i = 0; i < 30; i++) {
			// X, Y, Z positions
			float x = random.nextFloat()*800 - 400;
			float z = random.nextFloat() * -800;
			float y = terrain.getHeightOfTerrain(x, z);
			entities.add(new Entity(boxTexModel, new Vector3f(x, y, z),0,0,0,5));
		}
		for (int i = 0; i < 30; i++) {
			// X, Y, Z positions
			float x = random.nextFloat()*800 - 400;
			float z = random.nextFloat() * -800;
			float y = terrain.getHeightOfTerrain(x, z) + 5;
			entities.add(new Entity(boxTexModel, new Vector3f(x, y, z),0,0,0,5));
		}
		for (int i = 0; i < 300; i++) {
			// X, Y, Z positions
			float x = random.nextFloat()*800 - 400;
			float z = random.nextFloat() * -800;
			float y = terrain.getHeightOfTerrain(x, z);
			entities.add(new Entity(flowerModel, new Vector3f(x, y, z),0,0,0,1));
			entities.add(new Entity(fernModel, random.nextInt(4), new Vector3f(x, y, z),0,0,0,1));
		}
		for (int i = 0; i < 800; i++) {
			// X, Y, Z positions
			float x = random.nextFloat()*800 - 400;
			float z = random.nextFloat() * -800;
			float y = terrain.getHeightOfTerrain(x, z);
			entities.add(new Entity(fernModel, random.nextInt(4), new Vector3f(x, y, z),0,0,0,1));
		}
		
		// Create the master renderer object to render multiple objects
		// efficiently with one draw call for every object per frame.
		MasterRenderer renderer = new MasterRenderer();
		
		// GUI Renderer object
		GuiRenderer guiRenderer = new GuiRenderer(loader);
		
		while(!Display.isCloseRequested()){
			// Game Logic
			camera.move();
			player.move(terrain);
			
			// Process the terrain entities
			renderer.processEntity(player);
			renderer.processTerrain(terrain);
			renderer.processTerrain(terrain2);
			
			// Loop through all dragons to render
			for (Entity entity : entities) {
				// Every single entity to be rendered must go through this call
				// to process entity.
				renderer.processEntity(entity);
			}
			
			// Once a frame call the render method
			renderer.render(lights, camera);
			guiRenderer.render(guis);
			DisplayManager.updateDisplay();			
		}
		
		guiRenderer.cleanUp();
		renderer.cleanpUp();
		loader.cleanUp();
		DisplayManager.closeDisplay();

	}
}
