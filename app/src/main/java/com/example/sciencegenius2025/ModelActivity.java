package com.example.sciencegenius2025;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;


import com.google.ar.core.Anchor;
import com.google.ar.core.Pose;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class ModelActivity extends AppCompatActivity {

    private ArFragment arFragment;
    private final ModelRenderable[] models = new ModelRenderable[9];
    private final List<AnchorNode> placedNodes = new ArrayList<>();
    private GestureDetector gestureDetector;
    private TransformableNode lastSelectedNode;
    private ObjectAnimator rotationAnimator;
    private boolean isRotating = false;




    @SuppressLint("ObjectAnimatorBinding")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);

        // Gesture detector for swipe rotation
        gestureDetector = new GestureDetector(this, new GestureListener());
        arFragment.getArSceneView().getScene().setOnTouchListener((hitTestResult, motionEvent) -> {
            gestureDetector.onTouchEvent(motionEvent);
            return false;
        });

        // Load 9 models
        for (int i = 0; i < 9; i++) {
            int index = i;
            loadModel("model" + (i + 1) + ".glb", renderable -> models[index] = renderable);
        }

        // Model buttons
        for (int i = 0; i < 9; i++) {
            int modelIndex = i;
            int buttonId = getResources().getIdentifier("btn_model" + (i + 1), "id", getPackageName());
            Button modelButton = findViewById(buttonId);
            modelButton.setOnClickListener(v -> {
                if (models[modelIndex] != null) {
                    placeModel(models[modelIndex], 0f);
                } else {
                    Toast.makeText(this, "Model " + (modelIndex + 1) + " not loaded yet", Toast.LENGTH_SHORT).show();
                }
            });
        }

        Button clearButton = findViewById(R.id.btn_clear_models);
        clearButton.setOnClickListener(v -> {
            for (AnchorNode node : placedNodes) {
                node.getAnchor().detach();
                node.setParent(null);
            }
            placedNodes.clear();
            lastSelectedNode = null;

            isRotating = false;
            if (rotationAnimator != null) {
                rotationAnimator.cancel();
            }

            Toast.makeText(this, "All models cleared", Toast.LENGTH_SHORT).show();
        });

        MaterialButton rotateButton = findViewById(R.id.btn_rotate);
        rotateButton.setOnClickListener(v -> {
            if (lastSelectedNode == null) {
                Toast.makeText(this, "No model selected", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isRotating) {
                if (rotationAnimator != null) {
                    rotationAnimator.cancel();
                }
                rotateButton.setText("Rotate");
                isRotating = false;
            } else {
                rotationAnimator = ObjectAnimator.ofFloat(lastSelectedNode, "localRotation", 0f, 360f);
                rotationAnimator.setDuration(4000);
                rotationAnimator.setRepeatCount(ValueAnimator.INFINITE);
                rotationAnimator.setRepeatMode(ValueAnimator.RESTART);
                rotationAnimator.addUpdateListener(animation -> {
                    float animatedValue = (float) animation.getAnimatedValue();
                    lastSelectedNode.setLocalRotation(
                            com.google.ar.sceneform.math.Quaternion.axisAngle(
                                    new com.google.ar.sceneform.math.Vector3(0f, 1f, 0f),
                                    animatedValue
                            )
                    );
                });
                rotationAnimator.start();
                rotateButton.setText("Stop");
                isRotating = true;
            }
        });

        MaterialButton exitButton = findViewById(R.id.btn_exit);
        exitButton.setOnClickListener(v -> {
            finish(); // exits the current activity
        });


        uploadModelsToFirestore(); // TEMPORARY — call only once to upload metadata
    }

    private void uploadModelsToFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        for (int i = 1; i <= 9; i++) {
            String modelName = "model" + i + ".glb";
            String category;

            if (i <= 3) {
                category = "Human";
            } else if (i <= 6) {
                category = "Microorganism";
            } else {
                category = "Solar System";
            }

            Map<String, Object> modelData = new HashMap<>();
            modelData.put("Model_Name", modelName);
            modelData.put("Model_Category", category);

            String documentId = "model" + i; // <- Custom document name

            db.collection("models")
                    .document(documentId)
                    .set(modelData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("FirestoreUpload", "Model " + documentId + " uploaded successfully");
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirestoreUpload", "Error uploading " + documentId, e);
                    });
        }
    }



    private void loadModel(String filename, java.util.function.Consumer<ModelRenderable> callback) {
        // Firebase Storage reference
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference modelRef = storage.getReference().child("models/" + filename);

        try {
            // Temp file to store downloaded model
            File localFile = File.createTempFile("temp_model", ".glb");

            modelRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                Uri modelUri = Uri.fromFile(localFile);

                ModelRenderable.builder()
                        .setSource(this, modelUri)
                        .setIsFilamentGltf(true)
                        .build()
                        .thenAccept(callback)
                        .exceptionally(throwable -> {
                            Log.e("ModelActivity", "Renderable build failed: " + filename, throwable);
                            runOnUiThread(() -> Toast.makeText(this, "Failed to build model: " + filename, Toast.LENGTH_SHORT).show());
                            return null;
                        });

            }).addOnFailureListener(e -> {
                Log.e("ModelActivity", "Failed to download: " + filename, e);
                Toast.makeText(this, "Download failed: " + filename, Toast.LENGTH_SHORT).show();
            });

        } catch (IOException e) {
            Log.e("ModelActivity", "File creation failed", e);
        }
    }

    private void placeModel(ModelRenderable renderable, float offsetX) {
        if (arFragment.getArSceneView().getArFrame() == null) {
            Toast.makeText(this, "AR Frame not ready", Toast.LENGTH_SHORT).show();
            return;
        }

        Pose camPose = arFragment.getArSceneView().getArFrame().getCamera().getPose();
        float[] forward = camPose.getZAxis();

        float[] pos = camPose.getTranslation();
        float distance = 1.0f;

        Anchor anchor = arFragment.getArSceneView().getSession().createAnchor(
                Pose.makeTranslation(
                        pos[0] - forward[0] * distance + offsetX,
                        pos[1] - forward[1] * distance,
                        pos[2] - forward[2] * distance
                )
        );

        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());
        placedNodes.add(anchorNode);

        TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
        node.setParent(anchorNode);
        node.setRenderable(renderable);
        lastSelectedNode = node;

        node.setLocalRotation(com.google.ar.sceneform.math.Quaternion.axisAngle(
                new com.google.ar.sceneform.math.Vector3(0f, 1f, 0f), 0f));

        if (node.getScaleController() != null) {
            node.getScaleController().setMinScale(0.05f);
            node.getScaleController().setMaxScale(1.0f);
        }
        // Set initial smaller scale (adjust values as needed)
        node.setLocalScale(new com.google.ar.sceneform.math.Vector3(0.2f, 0.2f, 0.2f));
        node.select();


    }

    // Gesture detector for swipe left/right
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 50;
        private static final int SWIPE_VELOCITY_THRESHOLD = 50;

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (lastSelectedNode != null) {
                float rotationAmount = -distanceX * 0.1f; // adjust sensitivity
                com.google.ar.sceneform.math.Quaternion deltaRotation =
                        com.google.ar.sceneform.math.Quaternion.axisAngle(
                                new com.google.ar.sceneform.math.Vector3(0f, 1f, 0f),
                                rotationAmount
                        );

                com.google.ar.sceneform.math.Quaternion currentRotation = lastSelectedNode.getLocalRotation();
                com.google.ar.sceneform.math.Quaternion newRotation =
                        com.google.ar.sceneform.math.Quaternion.multiply(currentRotation, deltaRotation);

                lastSelectedNode.setLocalRotation(newRotation);
            }
            return true;
        }

    }
}