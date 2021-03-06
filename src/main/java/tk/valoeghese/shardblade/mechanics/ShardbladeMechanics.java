package tk.valoeghese.shardblade.mechanics;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import tk.valoeghese.shardblade.item.IShardblade;

public final class ShardbladeMechanics {
	private ShardbladeMechanics() {
	}

	public static boolean onAttack(PlayerEntity self, Entity target) {
		if (target.isAttackable()) {
			Item itemHeld = self.getStackInHand(Hand.MAIN_HAND).getItem();

			if (itemHeld instanceof IShardblade) {
				//				IShardblade blade = (IShardblade) itemHeld;

				List<LivingEntity> list = self.world.getNonSpectatingEntities(LivingEntity.class, target.getBoundingBox().expand(1.0D, 0.25D, 1.0D));

				if (target instanceof LivingEntity) {
					if (!list.contains(target)) { // should be false
						list.add((LivingEntity) target);
					}
				} else {
					target.kill();
				}

				if (self.world instanceof ServerWorld) {
					list.forEach(le -> {
						boolean isIncapacitated = ((IShardbladeAffectedEntity) le).isIncapacitatedByShardblade();

						if (isIncapacitated) {
							double x = target.getX();
							double y = target.getEyeY();
							double z = target.getZ();
							spawnSmoke(x, y, z, (ServerWorld) self.world);

							target.kill();
						} else {
							ItemStack leMainHandStack = le.getMainHandStack();

							if (!leMainHandStack.isEmpty()) {
								if (leMainHandStack.getCount() == 1) {
									if (le.getRandom().nextBoolean()) {
										le.dropStack(leMainHandStack);
										le.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
										return;
									}
								}
							}

							double x = target.getX();
							double y = target.getY();
							double z = target.getZ();
							spawnSmoke(x, y, z, (ServerWorld) self.world);

							le.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 999999, 40, false, true));
							((IShardbladeAffectedEntity) le).setIncapacitatedByShardblade(true);
						}
					});
				}

				return true;
			}
		}

		return false;
	}

	private static void spawnSmoke(double x, double y, double z, ServerWorld world) {
		world.spawnParticles(ParticleTypes.SMOKE, x, y, z, 15, 0.0D, 0.1D, 0.0D, 0.02D);
	}
}
