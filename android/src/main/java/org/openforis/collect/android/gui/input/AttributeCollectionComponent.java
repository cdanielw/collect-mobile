package org.openforis.collect.android.gui.input;

import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import org.openforis.collect.R;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.viewmodel.*;

import java.util.Map;
import java.util.Set;

/**
 * @author Daniel Wiell
 */
public abstract class AttributeCollectionComponent extends SavableComponent {
    protected final UiAttributeCollection attributeCollection;

    protected AttributeCollectionComponent(UiAttributeCollection attributeCollection, SurveyService surveyService, FragmentActivity context) {
        super(surveyService, context);
        this.attributeCollection = attributeCollection;
    }

    /**
     * Provide an click listener to get the add button next to the label.
     */
    protected View.OnClickListener onAddAttribute() {
        return null; // No add button by default
    }

    protected abstract void setValidationError(UiAttribute attribute, Set<UiValidationError> validationErrors);

    /**
     * Updates the {@code UiAttributeCollection} {@code UiAttribute} values that are different from the user input.
     *
     * @return the changed attributes.
     */
    protected abstract Set<UiAttribute> updateChangedAttributes();

    public final void onAttributeChange(UiAttribute attribute, Map<UiAttribute, UiAttributeChange> attributeChanges) {
        for (UiAttribute changedAttribute : attributeChanges.keySet())
            if (isInAttributeCollection(changedAttribute)) {
                Set<UiValidationError> validationErrors = attributeChanges.get(changedAttribute).validationErrors;
                if (validationErrors != null)
                    setValidationError(attribute, validationErrors);
            }
    }

    public int getViewResource() {
        return onAddAttribute() == null
                ? R.layout.fragment_attribute_detail
                : R.layout.attribute_collection_detail;
    }

    public void setupView(ViewGroup view) {
        super.setupView(view);
        View.OnClickListener addListener = onAddAttribute();
        if (addListener == null)
            return;

        final View addAttributeButton = view.findViewById(R.id.action_add_node);
        if (addAttributeButton == null)
            throw new IllegalStateException(getClass().getSimpleName() +
                    " specifies onAddAttribute, but view doesn't contain button with id action_add_node");
        addAttributeButton.setOnClickListener(addListener);
    }

    protected final void notifyAboutAttributeCollectionChange(Set<UiAttribute> changedAttributes) {
        surveyService.updateAttributes(changedAttributes);
    }

    public final void saveNode() {
        resetValidationErrors();
        notifyAboutAttributeCollectionChange(updateChangedAttributes());
    }

    public final void validateNode() {
        for (UiNode uiNode : attributeCollection.getChildren())
            validateAttribute((UiAttribute) uiNode);
    }

    private boolean isInAttributeCollection(UiAttribute attribute) {
        return attributeCollection.containsChildWithId(attribute.getId());
    }

    private void validateAttribute(UiAttribute attribute) {
        Set<UiValidationError> validationErrors = attribute.getValidationErrors();
        if (validationErrors != null)
            setValidationError(attribute, validationErrors);
    }

    public String toString() {
        return getClass().getSimpleName() + " for " + attributeCollection;
    }
}