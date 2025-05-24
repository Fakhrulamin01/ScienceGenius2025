package com.example.sciencegenius2025;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.Anchor;
import com.google.ar.core.Pose;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import android.view.GestureDetector;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelActivity extends AppCompatActivity {

    private ArFragment arFragment;
    private final List<AnchorNode> placedNodes = new ArrayList<>();
    private GestureDetector gestureDetector;
    private TransformableNode lastSelectedNode;
    private final Map<String, ModelData> modelDataMap = new HashMap<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);

        // Swipe gesture
        gestureDetector = new GestureDetector(this, new GestureListener());
        arFragment.getArSceneView().getScene().setOnTouchListener((hitTestResult, motionEvent) -> {
            gestureDetector.onTouchEvent(motionEvent);
            return false;
        });

        // Load models from Firestore
        loadModelsFromFirestore();

        // Clear button
        Button clearButton = findViewById(R.id.btn_clear_models);
        clearButton.setOnClickListener(v -> {
            for (AnchorNode node : placedNodes) {
                if (node.getAnchor() != null) {
                    node.getAnchor().detach();
                }
                node.setParent(null);
            }
            placedNodes.clear();
            lastSelectedNode = null;
            Toast.makeText(this, "All models cleared", Toast.LENGTH_SHORT).show();
        });

    }

    private void loadModelsFromFirestore() {
        CollectionReference modelsRef = db.collection("Models");

        modelsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    String modelId = doc.getString("Model_ID");
                    String modelName = doc.getString("Model_Name");
                    String filename = doc.getString("filename");

                    loadModel(filename, renderable -> {
                        ModelData data = new ModelData();
                        data.modelId = modelId;
                        data.modelName = modelName;
                        data.filename = filename;
                        data.renderable = renderable;

                        modelDataMap.put(modelId, data);

                        // Assign button if it exists
                        int buttonId = getResources().getIdentifier("btn_" + modelId.toLowerCase(), "id", getPackageName());
                        Button modelButton = findViewById(buttonId);
                        if (modelButton != null) {
                            modelButton.setOnClickListener(v -> {
                                if (data.renderable != null) {
                                    placeModel(data.renderable);
                                } else {
                                    Toast.makeText(this, "Model not ready", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }
            } else {
                Log.e("ModelActivity", "Failed to get models from Firestore", task.getException());
            }
        });
    }

    private void loadModel(String filename, java.util.function.Consumer<ModelRenderable> callback) {
        ModelRenderable.builder()
                .setSource(this, Uri.parse(filename))
                .setIsFilamentGltf(true)
                .build()
                .thenAccept(callback)
                .exceptionally(throwable -> {
                    Log.e("ModelActivity", "Failed to load model: " + filename, throwable);
                    runOnUiThread(() -> Toast.makeText(this, "Failed to load " + filename, Toast.LENGTH_SHORT).show());
                    return null;
                });
    }

    private void placeModel(ModelRenderable renderable) {
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
                        pos[0] - forward[0] * distance,
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

        if (node.getScaleController() != null) {
            node.getScaleController().setMinScale(0.1f);
            node.getScaleController().setMaxScale(5.0f);
        }

        node.select();
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (lastSelectedNode != null) {
                float rotationAmount = -distanceX * 0.1f;
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
