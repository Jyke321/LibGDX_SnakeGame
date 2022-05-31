package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

import java.awt.font.GlyphVector;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Iterator;

public class Snake extends ApplicationAdapter {

	private Texture snakeSegmentImage;
	private Texture appleImage;
	private Texture numbers;
	private Sprite[] score;

	private SpriteBatch spriteBatch;

	private OrthographicCamera camera;
	private SpriteBatch batch;

	private Rectangle apple;
	private ArrayDeque<Rectangle> snakeSegment;

	private int length;
	private boolean start;
	private char dir;

	private int scoreTally;
	private long lastScreenRefresh;
	private final int maxDigits = 100;

	@Override
	public void create () {
		snakeSegmentImage = new Texture(Gdx.files.internal("snake.png"));
		appleImage = new Texture(Gdx.files.internal("apple.png"));
		numbers = new Texture(Gdx.files.internal("numbers(0).png"));
		score = new Sprite[maxDigits];

		//create camera
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);

		batch = new SpriteBatch();

		Rectangle firstSegment = new Rectangle();
		firstSegment.x = 800/2;
		firstSegment.y = 480/2;
		firstSegment.width = 16;
		firstSegment.height = 16;

		snakeSegment = new ArrayDeque<>();
		snakeSegment.add(firstSegment);

		spawnApple();

		length = 6;
		start = false;
		dir = ' ';

		scoreTally = 0;
		lastScreenRefresh = System.currentTimeMillis();
	}

	private void spawnApple() {
		apple = new Rectangle();
		apple.x = Math.round(MathUtils.random(0,(800-16))/16)*16;
		apple.y = Math.round(MathUtils.random(0,(480-16))/16)*16;
		apple.width = 16;
		apple.height = 16;
	}

	@Override
	public void render () {
		ScreenUtils.clear(0, 0, 0, 1);
		batch.setProjectionMatrix(camera.combined);

		batch.begin();
		batch.draw(appleImage,apple.x,apple.y);
		for (Rectangle segment:snakeSegment) {
			batch.draw(snakeSegmentImage,segment.x,segment.y);
		}
		int scoreDigits = (int)Math.floor(Math.log10(scoreTally)) + 1;
		int scoreOffset = 8 * scoreDigits;
		for (int i = 0; i < scoreDigits; i++) {
			score[i] = new Sprite(numbers,16,16);
			int digit = (int) Math.floor((scoreTally/Math.pow(10,i))%10);
			score[i].setRegion(scoreSpriteX(digit),scoreSpriteY(digit),16,16);
			score[i].setPosition((400+scoreOffset-(16*i)),480-24);
			score[i].draw(batch);
		}
		batch.end();

		//handle input
		if(Gdx.input.isKeyPressed(Input.Keys.UP)||Gdx.input.isKeyPressed(Input.Keys.W)) {
			dir = 'W';
		}
		if(Gdx.input.isKeyPressed(Input.Keys.LEFT)||Gdx.input.isKeyPressed(Input.Keys.A)) {
			dir = 'A';
		}
		if(Gdx.input.isKeyPressed(Input.Keys.DOWN)||Gdx.input.isKeyPressed(Input.Keys.S))
			dir = 'S';
		if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)||Gdx.input.isKeyPressed(Input.Keys.D))
			dir = 'D';
		if(Gdx.input.isKeyPressed(Input.Keys.SPACE))
			scoreTally+=1;

		//checks if user has input movement key
		if (dir!=' ') start = true;

		//checks to update snake body
		if (start && (System.currentTimeMillis() - lastScreenRefresh > 100)) {
			lastScreenRefresh = System.currentTimeMillis();
			//create new segment
			Rectangle newSegment = new Rectangle();
			newSegment.x = snakeSegment.peekLast().x + changeX();
			newSegment.y = snakeSegment.peekLast().y + changeY();
			if(newSegment.x<0) newSegment.x = 800-16;
			if(newSegment.x>800-16) newSegment.x = 0;
			if(newSegment.y<0) newSegment.y = 480-16;
			if(newSegment.y>480-16) newSegment.y = 0;
			newSegment.width = 16;
			newSegment.height = 16;

			snakeSegment.addLast(newSegment);

			//delete segment
			if (snakeSegment.size()>=length) {
				snakeSegment.pollFirst();
			}
		}

		//check self collision
		HashSet<Vector2> snakePositions = new HashSet<>();
		for (Iterator<Rectangle> iter = snakeSegment.iterator(); iter.hasNext(); ) {
			Rectangle segment= iter.next();
			Vector2 pos = new Vector2();
			snakePositions.add(segment.getPosition(pos));
		}
		if (snakeSegment.size()>snakePositions.size()) {
			gameover();
		}

		//check for apple
		if (snakeSegment.peekLast().overlaps(apple)) {
			spawnApple();
			length += 1;
			scoreTally += 1;
		}
	}

	private int scoreSpriteX(int digit) {
		return 16 * (digit%3);
	}
	private int scoreSpriteY(int digit) {
		return 16 * (digit/3);
	}

	private void gameover() {
		System.exit(0);
	}

	private int changeY() {
		if (dir =='S') return -16;
		else if (dir == 'W') return  16;
		else return 0;
	}

	private int changeX() {
		if (dir =='A') return -16;
		else if (dir == 'D') return  16;
		else return 0;
	}

	@Override
	public void dispose () {
		appleImage.dispose();
		snakeSegmentImage.dispose();
		numbers.dispose();
		batch.dispose();
	}
}
