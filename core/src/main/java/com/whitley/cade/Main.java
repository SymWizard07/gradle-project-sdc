package com.whitley.cade;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.Input;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private ShapeRenderer shapeRenderer;
    private Ship ship;

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        ship = new Ship();
        ship.position.set(
            Gdx.graphics.getWidth() / 2f,
            Gdx.graphics.getHeight() / 2f
        );
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float delta = Gdx.graphics.getDeltaTime();
        handleInput(delta);
        ship.update(delta);
        drawShip();
    }

    private void handleInput(float delta) {
        // Rotation
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            ship.rotation += ship.ROTATION_SPEED * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            ship.rotation -= ship.ROTATION_SPEED * delta;
        }

        // Thrust
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            float radians = MathUtils.degreesToRadians * ship.rotation;
            ship.acceleration.set(
                MathUtils.cos(radians) * ship.THRUST_FORCE,
                MathUtils.sin(radians) * ship.THRUST_FORCE
            );
        } else {
            ship.acceleration.setZero();
        }
    }

    private void drawShip() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        
        // Calculate triangle vertices
        float[] vertices = ship.getTransformedVertices();
        shapeRenderer.triangle(
            vertices[0], vertices[1],
            vertices[2], vertices[3],
            vertices[4], vertices[5]
        );
        
        shapeRenderer.end();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }

    class Ship {
        public final float THRUST_FORCE = 200f;
        public final float ROTATION_SPEED = 180f; // degrees per second
        
        public Vector2 position = new Vector2();
        public Vector2 velocity = new Vector2();
        public Vector2 acceleration = new Vector2();
        public float rotation = 0;
        
        private final float[] baseVertices = new float[] {
            20, 0,
            -10, -10,
            -10, 10
        };

        public void update(float delta) {
            // Update velocity and position
            velocity.add(acceleration.x * delta, acceleration.y * delta);
            
            position.mulAdd(velocity, delta);
            
            // Screen wrapping
            wrapPosition();
        }

        private void wrapPosition() {
            float screenWidth = Gdx.graphics.getWidth();
            float screenHeight = Gdx.graphics.getHeight();
            
            if (position.x < 0) position.x += screenWidth;
            if (position.x > screenWidth) position.x -= screenWidth;
            if (position.y < 0) position.y += screenHeight;
            if (position.y > screenHeight) position.y -= screenHeight;
        }

        public float[] getTransformedVertices() {
            float[] transformed = new float[6];
            float radians = MathUtils.degreesToRadians * rotation;
            float cos = MathUtils.cos(radians);
            float sin = MathUtils.sin(radians);
            
            // Transform each vertex
            for (int i = 0; i < 3; i++) {
                float x = baseVertices[i*2];
                float y = baseVertices[i*2+1];
                
                // Rotate
                float tempX = x * cos - y * sin;
                float tempY = x * sin + y * cos;
                
                // Translate
                transformed[i*2] = tempX + position.x;
                transformed[i*2+1] = tempY + position.y;
            }
            
            return transformed;
        }
    }
}