// Generated by view binder compiler. Do not edit!
package com.example.collarcompanionv3.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.collarcompanionv3.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityConnectBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final ImageButton imageButton2;

  @NonNull
  public final TextView textView5;

  private ActivityConnectBinding(@NonNull ConstraintLayout rootView,
      @NonNull ImageButton imageButton2, @NonNull TextView textView5) {
    this.rootView = rootView;
    this.imageButton2 = imageButton2;
    this.textView5 = textView5;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityConnectBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityConnectBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_connect, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityConnectBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.imageButton2;
      ImageButton imageButton2 = ViewBindings.findChildViewById(rootView, id);
      if (imageButton2 == null) {
        break missingId;
      }

      id = R.id.textView5;
      TextView textView5 = ViewBindings.findChildViewById(rootView, id);
      if (textView5 == null) {
        break missingId;
      }

      return new ActivityConnectBinding((ConstraintLayout) rootView, imageButton2, textView5);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}