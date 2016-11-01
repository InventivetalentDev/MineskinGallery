package org.inventivetalent.mineskingallery;

import org.bukkit.inventory.meta.SkullMeta;
import org.inventivetalent.mcwrapper.auth.GameProfileWrapper;
import org.inventivetalent.mcwrapper.auth.properties.PropertyWrapper;
import org.inventivetalent.reflection.resolver.*;
import org.inventivetalent.reflection.resolver.minecraft.NMSClassResolver;
import org.inventivetalent.reflection.resolver.minecraft.OBCClassResolver;

import java.lang.reflect.Field;
import java.util.UUID;

public class HeadTextureChanger {

	static final ClassResolver    classResolver    = new ClassResolver();
	static final NMSClassResolver nmsClassResolver = new NMSClassResolver();
	static final OBCClassResolver obcClassResolver = new OBCClassResolver();

	static Class<?> NBTTagCompound        = nmsClassResolver.resolveSilent("NBTTagCompound");
	static Class<?> NBTBase               = nmsClassResolver.resolveSilent("NBTBase");
	static Class<?> GameProfileSerializer = nmsClassResolver.resolveSilent("GameProfileSerializer");
	static Class<?> CraftMetaSkull        = obcClassResolver.resolveSilent("inventory.CraftMetaSkull");

	static Class<?> GameProfile = classResolver.resolveSilent("net.minecraft.util.com.mojang.authlib.GameProfile", "com.mojang.authlib.GameProfile");

	static final MethodResolver NBTTagCompoundMethodResolver        = new MethodResolver(NBTTagCompound);
	static final MethodResolver GameProfileSerializerMethodResolver = new MethodResolver(GameProfileSerializer);

	static final FieldResolver CraftMetaSkullFieldResolver = new FieldResolver(CraftMetaSkull);

	static final ConstructorResolver CraftMetaSkullConstructorResolver = new ConstructorResolver(CraftMetaSkull);

	public static Object createProfile(String data) {
		try {
			GameProfileWrapper profileWrapper = new GameProfileWrapper(UUID.randomUUID(), "CustomBlock");
			PropertyWrapper propertyWrapper = new PropertyWrapper("textures", data);
			profileWrapper.getProperties().put("textures", propertyWrapper);

			return profileWrapper.getHandle();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Object createProfile(String value, String signature) {
		if (signature == null) { return createProfile(value); }
		try {
			GameProfileWrapper profileWrapper = new GameProfileWrapper(UUID.randomUUID(), "CustomBlock");
			PropertyWrapper propertyWrapper = new PropertyWrapper("textures", value, signature);
			profileWrapper.getProperties().put("textures", propertyWrapper);

			return profileWrapper.getHandle();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static SkullMeta applyTextureToMeta(SkullMeta meta, Object profile) throws Exception {
		if (meta == null) { throw new IllegalArgumentException("meta cannot be null"); }
		if (profile == null) { throw new IllegalArgumentException("profile cannot be null"); }
		Object baseNBTTag = NBTTagCompound.newInstance();
		Object ownerNBTTag = NBTTagCompound.newInstance();

		GameProfileSerializerMethodResolver.resolve(new ResolverQuery("serialize", NBTTagCompound, GameProfile)).invoke(null, ownerNBTTag, profile);
		NBTTagCompoundMethodResolver.resolve(new ResolverQuery("set", String.class, NBTBase)).invoke(baseNBTTag, "SkullOwner", ownerNBTTag);

		SkullMeta newMeta = (SkullMeta) CraftMetaSkullConstructorResolver.resolve(new Class[] { NBTTagCompound }).newInstance(baseNBTTag);

		Field profileField = CraftMetaSkullFieldResolver.resolve("profile");
		profileField.set(meta, profile);
		profileField.set(newMeta, profile);

		return newMeta;
	}

}