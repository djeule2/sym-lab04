package ch.heigvd.iict.sym_labo4.gl.objects;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

public class ObjLoader{

    private static final String TAG = ObjLoader.class.getSimpleName();

	private List<Mesh> meshes = new LinkedList<>();
    private List<Material> materials = new LinkedList<>();

	public ObjLoader(InputStream inModel) {

        List<String> vertices = new ArrayList<>();
        List<String> faces = new ArrayList<>();
		Material currentMaterial = null;
		
		List<PriorMesh> pMeshes = new LinkedList<>();

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(inModel));

			String line = in.readLine();

			while(line != null){
				if(line.startsWith("v ")){
					vertices.add(line.substring(2));
				}
				else if(line.startsWith("f ")){
					faces.add(line.substring(2));
				}
				else if(line.startsWith("g ")){
					//next line contains the material to use
					List<String> v = null, f = null;

					if(vertices.size() > 0 && faces.size() > 0 && currentMaterial != null){
						v = vertices;
						f = faces;
						pMeshes.add(createMesh(v, f, currentMaterial));
						vertices = new ArrayList<>();
						faces = new ArrayList<>();
					}

					line = in.readLine();
                    currentMaterial = null;
					String matName = line.split(" ")[1];
					for(Material m: this.materials){
						if(m.getName().equalsIgnoreCase(matName)){
							currentMaterial = m;
							break;
						}
					}
					if(currentMaterial == null){
						currentMaterial = getRandomMaterial();
					}
				}

				line = in.readLine();
			}
			pMeshes.add(createMesh(vertices, faces, currentMaterial));

			//re-center of the object
			int nbr = 0, totX = 0, totY = 0, totZ = 0;
			for(PriorMesh pm: pMeshes){
				float[] coord = pm.getVertices();
				for(int i = 0; i < coord.length; ++i){
					if(i%3 == 0){
						totX += coord[i];
						nbr += 1;
					}
					else if(i%3 == 1){
						totY += coord[i];
					}
					else if(i%3 == 2){
						totZ += coord[i];
					}
				}
			}
			float mX = (float)((double)(totX) / (double)(nbr));
			float mY = (float)((double)(totY) / (double)(nbr));
			float mZ = (float)((double)(totZ) / (double)(nbr));

			for(PriorMesh pm: pMeshes){
				Mesh m = new Mesh();
				pm.recenter(mX, mY, mZ);
				m.setVertices(pm.getVertices());
				m.setIndices(pm.getIndices());
				m.setColors(pm.getColors());

				meshes.add(m);
			}

		} catch (IOException e) {
            Log.w(TAG, "Exception while reading .obj file", e);
        }

	}
	
	private PriorMesh createMesh(List<String> vertices, List<String> faces, Material mat){
		
		PriorMesh tmpMesh = new PriorMesh();
		
		float[] vert = new float[3 * vertices.size()];
		short[] ind  = new short[3 * faces.size()];
		float[] colors = new float[4 * vertices.size()];
		
		for(int i = 0; i < vertices.size(); ++i){
			String[] coord = vertices.get(i).split(" ");
			vert[3*i + 0] = Float.parseFloat(coord[0]);
			vert[3*i + 1] = Float.parseFloat(coord[1]);
			vert[3*i + 2] = Float.parseFloat(coord[2]);
		}
		
		for(int i = 0; i < faces.size(); ++i){
			String[] coord = faces.get(i).split(" ");
			for(int j = 0; j < 3; ++j){
				String[] parts = coord[j].split("/");
				ind[3*i + j] = (short)(Short.parseShort(parts[0]) - 1);
			}
		}
		
		//correction of the indices
		short min = Short.MAX_VALUE;
		for(int i = 0; i < ind.length; ++i){
			if(ind[i] < min){
				min = ind[i];
			}
		}
		for(int i = 0; i < ind.length; ++i){
			ind[i] -= min;
		}
		
		//TODO use a real material
		float[] color = mat.getKd();
		
		for(int i = 0; i < colors.length; ++i){
			if(i%4 == 3){
				colors[i] = 1.0f;
			}
			else{
				colors[i] = color[i%4];
			}
		}
		
		tmpMesh.setColors(colors);
		tmpMesh.setVertices(vert);
		tmpMesh.setIndices(ind);
		
		return tmpMesh;
	}

	public void draw(GL10 gl){
		for(Mesh m: this.meshes){
			m.draw(gl);
		}
	}
	
	private Material getRandomMaterial() {
		Material m = new Material();
		Random r = new Random();
		
		float[] tmp = new float[3]; tmp[0] = r.nextFloat(); tmp[1] = r.nextFloat(); tmp[2] = r.nextFloat();
		m.setKa(tmp);
		tmp = new float[3]; tmp[0] = r.nextFloat(); tmp[1] = r.nextFloat(); tmp[2] = r.nextFloat();
		m.setKd(tmp);
		tmp = new float[3]; tmp[0] = r.nextFloat(); tmp[1] = r.nextFloat(); tmp[2] = r.nextFloat();
		m.setKs(tmp);
		
		m.setName("random auto-generated");
		
		return m;
	}

    private static class Material{
		
		String name = null;
		float[] kd = new float[3];
		float[] ks = new float[3];
		float[] ka = new float[3];
		
        String getName() {
			return name;
		}
        void setName(String name) {
			this.name = name;
		}
        float[] getKd() {
			return kd;
		}
        void setKd(float[] kd) {
			this.kd = kd;
		}
		public float[] getKs() {
			return ks;
		}
        void setKs(float[] ks) {
			this.ks = ks;
		}
        float[] getKa() {
			return ka;
		}
        void setKa(float[] ka) {
			this.ka = ka;
		}		
	}

	private static class PriorMesh{
		float[] vertices = null;
		short[] indices = null;
		float[] colors = null;
		
        void recenter(float mX, float mY, float mZ){
			for(int i = 0; i < this.vertices.length; ++i){
				if(i%3 == 0){
					this.vertices[i] -= mX;
				}
				else if(i%3 == 1){
					this.vertices[i] -= mY;
				}
				else if(i%3 == 2){
					this.vertices[i] -= mZ;
				}
			}
		}
		
        float[] getVertices() {
			return vertices;
		}
        void setVertices(float[] vertices) {
			this.vertices = vertices;
		}
        short[] getIndices() {
			return indices;
		}
        void setIndices(short[] indices) {
			this.indices = indices;
		}
        float[] getColors() {
			return colors;
		}
        void setColors(float[] colors) {
			this.colors = colors;
		}
	}

}
