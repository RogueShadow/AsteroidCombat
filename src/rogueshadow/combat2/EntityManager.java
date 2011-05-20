package rogueshadow.combat2;

import java.util.ArrayList;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Vector2f;

public class EntityManager {
	protected boolean paused = false;
	public final int ROCKS = 0;
	public final int BULLETS = 1;
	public final int POWERUPS = 2;
	public final int CHECKS = 3;
	
	protected int count[] = {0,0,0,0};
	protected GameContainer container;
	protected Combat2 game;
	protected ArrayList<Entity> entities = new ArrayList<Entity>();
	protected ArrayList<Entity> removeList = new ArrayList<Entity>();
	protected ArrayList<Entity> addList = new ArrayList<Entity>();
	
	public boolean isPaused() {
		return paused;
	}

	public void setPaused(boolean paused) {
		this.paused = paused;
	}
	
	public String debug = "";
	
	public EntityManager(GameContainer container, Combat2 game){
		this.container = container;
		this.game = game;
	}
	
	public void update(int delta){
		if (isPaused())return;
		
		
		for (int i = 0; i < count.length; i++)count[i] = 0;

		for (int i = 0; i < entities.size(); i++ ){
			Entity e = (Entity) entities.get(i);
			e.update(this, delta);
		}
		for (int i = 0; i < entities.size(); i++){
			
			Entity entity = (Entity) entities.get(i);
			
			if (entity instanceof Rock)count[ROCKS]++;
			if (entity instanceof Bullet)count[BULLETS]++;
			if (entity instanceof Powerup)count[POWERUPS]++;
			
			for (int j = i+1; j < entities.size(); j++){
				
				Entity other = (Entity) entities.get(j);
				
				CollisionInfo c = entity.collides(other);
				
				setClosestEntities(c);
				
				count[CHECKS]++;
				if (c.isCollided()){
					entity.collided(this, other);
					other.collided(this, entity);
				}
				
			}
		}
		
		entities.removeAll(removeList);
		entities.addAll(addList);
		removeList.clear();
		addList.clear();
		
		if (count[ROCKS] == 0){
			setPaused(true);
			game.clearedRound();
			
		}
		

		debug = "ChecksDone: " + Integer.toString(count[CHECKS]);
		
	}
	
	private void setClosestEntities(CollisionInfo c) {
		Rock rock;
		Bullet bullet;
		if (c.entity instanceof Rock && c.other instanceof Bullet){
			bullet = (Bullet)c.other;
			if (!(bullet.getHoming() > 0))return;
			rock = (Rock)c.entity;
		}else
		if(c.entity instanceof Bullet && c.other instanceof Rock){
			bullet = (Bullet)c.entity;
			if (!(bullet.getHoming() > 0))return;
			rock = (Rock)c.other;
		}else return;
		
		if (c.distance < bullet.closest_dist){
			bullet.closest = rock;
			bullet.closest_dist = c.distance;
		}
	}

	public void render(Graphics g){
		for (int i = 0; i < entities.size(); i++ ){
			Entity e = (Entity) entities.get(i);
			e.render(g);
		}
		g.setColor(Color.white);
		g.drawString(debug, 10, 30);
	}
	
	public void remove(Entity other){
		other.setDestroyed(true);
		removeList.add(other);
	}
	public void add(Entity other){
		if (other instanceof Powerup){
			if (count[POWERUPS] >= Powerup.MAX_POWERUPS)return;
		}
		addList.add(other);
	}
	
	public void generateRocks(int round){
		ArrayList<Rock> rocks = new ArrayList<Rock>();
		Rock rock;
		float x, y, angle, speed;
		speed = 100f;
		int number = round * 10;
		int size = 6;
		while (number > 0){
			while (Math.pow(2, size) > number)size--;
			number -= Math.pow(2, size);
			if (Math.random() > 0.5){
				x = (float)(Math.random()*(container.getWidth()/3f));
			}else{
				x = container.getWidth()-(container.getWidth()/3f);
				x += (float)(Math.random()*(container.getWidth()/3f));
			}
			if (Math.random() > 0.5){
				y = (float)(Math.random()*(container.getHeight()/3f));
			}else{
				y = container.getHeight()-(container.getHeight()/3f);
				y += (float)(Math.random()*(container.getHeight()/3f));
			}
			angle = (float)(Math.random()*360);
			rock = new Rock(new Vector2f(x,y), new Vector2f(angle).scale(speed), size);
			rocks.add(rock);
		}
		entities.addAll( rocks );
	}

	public GameContainer getContainer() {
		return container;
	}

	public void setContainer(GameContainer container) {
		this.container = container;
	}

	public Combat2 getGame() {
		return game;
	}

	public void setGame(Combat2 game) {
		this.game = game;
	}
}