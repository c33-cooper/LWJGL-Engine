package engineTester;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.xml.crypto.Data;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import entities.Camera;
import entities.Entity;
import entities.Light;
import entities.Player;
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
		
		// Textured Models
		TexturedModel treeModel = new TexturedModel(treeStaticModel, new ModelTexture
				(loader.loadTexture("tree")));
		TexturedModel grassModel = new TexturedModel(grassStaticModel, new ModelTexture
				(loader.loadTexture("grassTexture")));
		TexturedModel fernModel = new TexturedModel(fernStaticModel, new ModelTexture
				(loader.loadTexture("fern")));
		TexturedModel flowerModel = new TexturedModel(flowerStaticModel, new ModelTexture
				(loader.loadTexture("flower")));
		TexturedModel personModel = new TexturedModel(playerStaticModel, new ModelTexture
				(loader.loadTexture("playerTexture")));
		TexturedModel lowPolyTreeTexModel = new TexturedModel(lowPolyTreeModel, new ModelTexture
				(loader.loadTexture("tree")));
		
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
		
		Light light = new Light(new Vector3f(20000, 20000, 20000), new Vector3f(1, 1, 1));
		
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
		
		// Player object
		Player player = new Player(personModel, new Vector3f(100, 0, -50), 0, -198.87997f, 0, 1);
		Camera camera = new Camera(player);
		
		//Entity entity = new Entity(staticModel, new Vector3f(0, 0, -50), 0, 0, 0, 1);
		// A list of all entities to be rendered in one draw call per frame.
		List<Entity> entities = new ArrayList<Entity>();
		Random random = new Random();
		
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
			entities.add(new Entity(fernModel, new Vector3f(x, y, z),0,0,0,1));
		}
		for (int i = 0; i < 300; i++) {
			// X, Y, Z positions
			float x = random.nextFloat()*800 - 400;
			float z = random.nextFloat() * -800;
			float y = terrain.getHeightOfTerrain(x, z);
			entities.add(new Entity(flowerModel, new Vector3f(x, y, z),0,0,0,1));
		}
		
		// Create the master renderer object to render multiple objects
		// efficiently with one draw call for every object per frame.
		MasterRenderer renderer = new MasterRenderer();
		
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
			renderer.render(light, camera);
			DisplayManager.updateDisplay();			
		}
		
		renderer.cleanpUp();
		loader.cleanUp();
		DisplayManager.closeDisplay();

	}
}
