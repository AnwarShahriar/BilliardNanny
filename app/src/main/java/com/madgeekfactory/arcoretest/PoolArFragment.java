package com.madgeekfactory.arcoretest;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.Arrays;

public class PoolArFragment extends ArFragment {

  private ModelRenderable cylinderRenderable;
  private AnchorNode lastAnchorNode;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setOnTapArPlaneListener(this::onTapArPlane);
    MaterialFactory.makeOpaqueWithColor(getContext(), new Color(android.graphics.Color.BLUE))
            .thenAccept(
                    material -> {
                      cylinderRenderable =
                              ShapeFactory.makeCylinder(0.02f, 0.005f, new Vector3(0.0f, 0.0f, 0.0f), material); });
  }

  private void onTapArPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
    Anchor anchor = hitResult.createAnchor();
    AnchorNode anchorNode = new AnchorNode(anchor);
    anchorNode.setParent(getArSceneView().getScene());

    TransformableNode hole = new TransformableNode(getTransformationSystem());
    hole.setParent(anchorNode);
    hole.setRenderable(cylinderRenderable);

    if (lastAnchorNode != null) {
      drawLineBetween(lastAnchorNode, anchorNode);
    } else {
      lastAnchorNode = anchorNode;
    }
  }

  // Reference: https://stackoverflow.com/questions/51951704/how-to-draw-line-between-two-anchors-in-sceneform-in-arcore
  private void drawLineBetween(AnchorNode source, AnchorNode target) {
    Vector3 point1 = source.getWorldPosition();
    Vector3 point2 = target.getWorldPosition();

    /*
     * First, find the vector extending between the two points and define a look rotation
     * in terms of this Vector.
     */
    final Vector3 difference = Vector3.subtract(point1, point2);
    final Vector3 directionFromTopToBottom = difference.normalized();
    final Quaternion rotationFromAToB =
            Quaternion.lookRotation(directionFromTopToBottom, Vector3.up());
    MaterialFactory.makeOpaqueWithColor(getContext(), new Color(0, 255, 244))
            .thenAccept(
                    material -> {
                            /* Then, create a rectangular prism, using ShapeFactory.makeCube() and use the difference vector
                                   to extend to the necessary length.  */
                      ModelRenderable model = ShapeFactory.makeCube(
                              new Vector3(.01f, .01f, difference.length()),
                              Vector3.zero(), material);
                            /* Last, set the world rotation of the node to the rotation calculated earlier and set the world position to
                                   the midpoint between the given points . */
                      Node node = new Node();
                      node.setParent(target);
                      node.setRenderable(model);
                      node.setWorldPosition(Vector3.add(point1, point2).scaled(.5f));
                      node.setWorldRotation(rotationFromAToB);
                      lastAnchorNode = null;
                    }
            );
  }
}