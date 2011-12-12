package com.dynamo.cr.tileeditor.scene;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;

import com.dynamo.cr.properties.GreaterThanZero;
import com.dynamo.cr.properties.NotEmpty;
import com.dynamo.cr.properties.Property;
import com.dynamo.cr.sceneed.core.Node;
import com.dynamo.cr.sceneed.core.validators.Unique;
import com.dynamo.cr.tileeditor.Activator;
import com.dynamo.tile.proto.Tile;
import com.dynamo.tile.proto.Tile.Playback2;

public class AnimationNode extends Node {

    @Property
    @NotEmpty
    @Unique
    private String id;

    @Property
    @GreaterThanZero
    private int startTile;

    @Property
    @GreaterThanZero
    private int endTile;

    @Property
    private Tile.Playback2 playback;

    @Property
    @GreaterThanZero
    private int fps;

    @Property
    private boolean flipHorizontal;

    @Property
    private boolean flipVertical;

    private int currentTile;
    private float cursor;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getStartTile() {
        return this.startTile;
    }

    public void setStartTile(int startTile) {
        this.startTile = startTile;
    }

    public IStatus validateStartTile() {
        if (getModel() != null) {
            TileSetNode tileSet = getTileSetNode();
            int tileCount = tileSet.calculateTileCount();
            if (this.startTile >= tileCount) {
                return new Status(IStatus.ERROR, Activator.PLUGIN_ID, NLS.bind(Messages.AnimationNode_startTile_INVALID, tileCount));
            }
        }
        return Status.OK_STATUS;
    }

    public TileSetNode getTileSetNode() {
        return (TileSetNode) getParent().getParent();
    }

    public int getEndTile() {
        return this.endTile;
    }

    public void setEndTile(int endTile) {
        this.endTile = endTile;
    }

    public IStatus validateEndTile() {
        if (getModel() != null) {
            TileSetNode tileSet = getTileSetNode();
            int tileCount = tileSet.calculateTileCount();
            if (this.endTile >= tileCount) {
                return new Status(IStatus.ERROR, Activator.PLUGIN_ID, NLS.bind(Messages.AnimationNode_endTile_INVALID, tileCount));
            }
        }
        return Status.OK_STATUS;
    }

    public Tile.Playback2 getPlayback() {
        return this.playback;
    }

    public void setPlayback(Tile.Playback2 playback) {
        this.playback = playback;
    }

    public int getFps() {
        return this.fps;
    }

    public void setFps(int fps) {
        this.fps = fps;
    }

    public boolean isFlipHorizontal() {
        return this.flipHorizontal;
    }

    public void setFlipHorizontal(boolean flipHorizontal) {
        this.flipHorizontal = flipHorizontal;
    }

    public boolean isFlipVertical() {
        return this.flipVertical;
    }

    public void setFlipVertical(boolean flipVertical) {
        this.flipVertical = flipVertical;
    }

    public int getCurrentTile() {
        return this.currentTile;
    }

    public void setCursor(float cursor) {
        this.cursor = cursor;
        if (this.playback != Playback2.PLAYBACK2_NONE) {
            int delta = (int)(cursor * this.fps);
            int tileCount = this.endTile - this.startTile + 1;
            boolean once = this.playback == Playback2.PLAYBACK2_ONCE_FORWARD || this.playback == Playback2.PLAYBACK2_ONCE_BACKWARD;
            if (once) {
                if (delta < 0) {
                    delta = 0;
                } else if (delta >= tileCount) {
                    delta = tileCount - 1;
                }
            } else if (this.playback == Playback2.PLAYBACK2_LOOP_PINGPONG) {
                // Length of one cycle, forward and backward
                int cycleLength = tileCount * 2 - 2;
                delta %= cycleLength;
                if (delta >= tileCount) {
                    delta = cycleLength - delta;
                }
            } else {
                delta %= tileCount;
            }
            boolean backwards = this.playback == Playback2.PLAYBACK2_ONCE_BACKWARD || this.playback == Playback2.PLAYBACK2_LOOP_BACKWARD;
            if (backwards) {
                delta = tileCount - 1 - delta;
            }
            this.currentTile = this.startTile + delta;
        } else {
            this.currentTile = this.startTile;
        }
    }

    public boolean hasFinished() {
        boolean once = this.playback == Playback2.PLAYBACK2_ONCE_FORWARD || this.playback == Playback2.PLAYBACK2_ONCE_BACKWARD;
        if (once) {
            int delta = (int)(cursor * this.fps);
            int tileCount = this.endTile - this.startTile + 1;
            return delta >= tileCount;
        } else if (this.playback == Playback2.PLAYBACK2_NONE) {
            return true;
        }
        return false;
    }

    @Override
    public Image getIcon() {
        return Activator.getDefault().getImageRegistry().get(Activator.ANIMATION_IMAGE_ID);
    }
}
