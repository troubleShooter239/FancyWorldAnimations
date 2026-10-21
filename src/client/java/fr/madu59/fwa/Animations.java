package fr.madu59.fwa;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import fr.madu59.fwa.anims.Animation;
import fr.madu59.fwa.mixin.client.SetSectionDirtyInvoker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.core.BlockPos;

public class Animations{

    public final Map<BlockPos, Animation> animations = new ConcurrentHashMap<>();
    
    public void removeAt(BlockPos blockPos) {
        this.animations.remove(blockPos);
    }

    public void removeSafeAt(BlockPos blockPos) {
        Animation animation = getAt(blockPos);
        if(animation == null) return;
        LevelExtractor levelExtractor = Minecraft.getInstance().levelExtractor;
        animation.markForRemoval();
		markSectionDirty(levelExtractor, blockPos);
    }

    public void clean(double nowTick) {
        ClientLevel level = Minecraft.getInstance().level;
        LevelExtractor levelExtractor = Minecraft.getInstance().levelExtractor;
        Iterator<Animation> it = this.animations.values().iterator();
        while (it.hasNext()) {
            Animation animation = it.next();
            BlockPos pos = animation.getPos();
            assert level != null;
            if(level.isLoaded(pos)){
                if (animation.isForRemoval()){
                    if(!(animation.hideOriginalBlock() || animation.hideOriginalBlockEntity()) || animation.isApprovedForRemoval(nowTick)) {
                        it.remove();
                    }
                }
                else if (animation.isFinished(nowTick)) {
                    animation.markForRemoval();
					markSectionDirty(levelExtractor, pos);
                }
            }
            else{
                it.remove();
            }
        }
    }

	private static void markSectionDirty(LevelExtractor levelExtractor, BlockPos pos) {
		((SetSectionDirtyInvoker) levelExtractor).fwa$setSectionDirty(
				pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4, true);
	}

    public boolean containsAt(BlockPos blockPos) {
        return this.animations.containsKey(blockPos);
    }

    public Animation getAt(BlockPos blockPos) {
        return this.animations.get(blockPos);
    }

    public void add(BlockPos pos, Animation anim) {
        animations.put(pos, anim);
    }

    public boolean isEmpty(){
        return animations.isEmpty();
    }

    public void clearModelCache(){
        for(Animation animation : this.animations.values()){
            animation.clearModelCache();
        }
    }
}
