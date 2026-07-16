package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public final class RackShortcutScreen extends AbstractContainerScreen<RackShortcutMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            KaleidoscopeGrilling.MOD_ID, "textures/gui/advanced_rack_shortcut.png");
    private static final int COMPARTMENT_BACKGROUND = 0x80666666;
    private static final ResourceLocation DEPOSIT_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            KaleidoscopeGrilling.MOD_ID, "textures/gui/advanced_rack_deposit.png");
    private static final ResourceLocation DEPOSIT_SELECTED_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            KaleidoscopeGrilling.MOD_ID, "textures/gui/advanced_rack_deposit_selected.png");
    private int hovered = -1;
    private boolean depositHovered;
    private int ticksOpen;

    public RackShortcutScreen(RackShortcutMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 194;
        imageHeight = 198;
    }

    @Override protected void init() {
        super.init();
        leftPos = width / 2 - 97;
        topPos = Math.max(0, Math.min(height / 2 - 72, height - imageHeight));
    }

    @Override protected void containerTick() {
        super.containerTick();
        if (!RackKeyHandler.isHeld()) {
            onClose();
            return;
        }
        ticksOpen++;
    }

    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
        hovered = at(mouseX, mouseY);
        depositHovered = isDepositAt(mouseX, mouseY);
        for (int i = 0; i < 9; i++) {
            int x = leftPos + menu.slots.get(i).x - 1;
            int y = topPos + menu.slots.get(i).y - 1;
            graphics.fill(x + 1, y + 1, x + 17, y + 17, COMPARTMENT_BACKGROUND);
            if (i == hovered) outline(graphics, x, y, 18, 18, 0xFFF0B957);
            else if (menu.rememberedSlot() == i) outline(graphics, x, y, 18, 18, 0xFF9A7135);
            if (menu.slots.get(i).getItem().isEmpty() && !menu.getFilter(i).isEmpty()) {
                graphics.renderItem(menu.getFilter(i), x + 1, y + 1);
                graphics.fill(x + 1, y + 1, x + 17, y + 17, 0x77101418);
            }
        }
        drawDeposit(graphics);
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        float progress = easedProgress(partialTick);
        double logicalMouseX = logicalMouseX(mouseX, progress);
        double logicalMouseY = logicalMouseY(mouseY, progress);
        graphics.pose().pushPose();
        graphics.pose().translate(width / 2.0F, height / 2.0F + (1 - progress) * 10, 0);
        float scale = 0.84F + progress * 0.16F;
        graphics.pose().scale(scale, scale, 1);
        graphics.pose().translate(-width / 2.0F, -height / 2.0F, 0);
        renderBg(graphics, partialTick, (int) Math.round(logicalMouseX), (int) Math.round(logicalMouseY));
        renderSlotItems(graphics);
        for (int i = 0; i < 9; i++) drawCount(graphics, i, menu.slots.get(i).getItem().getCount());
        graphics.pose().popPose();
        if (progress > .9F) {
            int slot = at(logicalMouseX, logicalMouseY);
            if (slot >= 0) {
                var stack = menu.slots.get(slot).getItem();
                if (stack.isEmpty()) stack = menu.getFilter(slot);
                if (!stack.isEmpty()) graphics.renderTooltip(font, stack, mouseX, mouseY);
            } else if (isDepositAt(logicalMouseX, logicalMouseY)) {
                graphics.renderTooltip(font, Component.translatable("gui.kaleidoscope_grilling.rack_deposit"), mouseX, mouseY);
            }
        }
    }

    @Override public boolean mouseClicked(double x, double y, int button) {float progress=easedProgress(0);double logicalX=logicalMouseX(x,progress),logicalY=logicalMouseY(y,progress);if(button==0&&isDepositAt(logicalX,logicalY)){send(RackShortcutMenu.DEPOSIT_BUTTON);onClose();return true;}int slot=at(logicalX,logicalY);if(button==0&&slot>=0&&menu.slots.get(slot).hasItem()){select(slot);return true;}return super.mouseClicked(logicalX,logicalY,button);}
    @Override public void mouseMoved(double x,double y){float progress=easedProgress(0);double logicalX=logicalMouseX(x,progress),logicalY=logicalMouseY(y,progress);hovered=at(logicalX,logicalY);depositHovered=isDepositAt(logicalX,logicalY);super.mouseMoved(logicalX,logicalY);}
    @Override public boolean keyPressed(int key,int scan,int mods){if(minecraft!=null)for(int i=0;i<9;i++)if(minecraft.options.keyHotbarSlots[i].matches(key,scan)&&menu.slots.get(i).hasItem()){select(i);return true;}return super.keyPressed(key,scan,mods);}
    @Override public boolean keyReleased(int key,int scan,int mods){if(RackKeyHandler.KEY.matches(key,scan)){RackKeyHandler.finishHold();if(depositHovered){send(RackShortcutMenu.DEPOSIT_BUTTON);onClose();}else if(hovered>=0&&menu.slots.get(hovered).hasItem())select(hovered);else onClose();return true;}return super.keyReleased(key,scan,mods);}

    private float easedProgress(float partialTick){float p=Mth.clamp((ticksOpen+partialTick)/9F,0F,1F);return 1F-(1F-p)*(1F-p)*(1F-p);}
    private double logicalMouseX(double mouseX,float progress){float scale=.84F+progress*.16F;return width/2.0+(mouseX-width/2.0)/scale;}
    private double logicalMouseY(double mouseY,float progress){float scale=.84F+progress*.16F,offset=(1-progress)*10;return height/2.0+(mouseY-height/2.0-offset)/scale;}
    private void select(int slot){send(slot);onClose();}
    private void send(int id){if(minecraft!=null&&minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}
    private int at(double mx,double my){for(int i=0;i<9;i++){int x=leftPos+menu.slots.get(i).x,y=topPos+menu.slots.get(i).y;if(mx>=x&&mx<x+16&&my>=y&&my<y+16)return i;}return-1;}
    private boolean isDepositAt(double mouseX,double mouseY){return mouseX>=leftPos+89&&mouseX<leftPos+105&&mouseY>=topPos+64&&mouseY<topPos+80;}
    private void drawCount(GuiGraphics g,int slot,int count){if(menu.getFilter(slot).isEmpty()&&menu.slots.get(slot).getItem().isEmpty())return;String text=Integer.toString(count);int x=leftPos+menu.slots.get(slot).x+16-font.width(text),y=topPos+menu.slots.get(slot).y+8;g.drawString(font,text,x,y,0xFFFFFFFF,true);}
    private void drawDeposit(GuiGraphics graphics){int x=leftPos+89,y=topPos+64;graphics.blit(depositHovered?DEPOSIT_SELECTED_TEXTURE:DEPOSIT_TEXTURE,x,y,0,0,16,16,16,16);}
    private void renderSlotItems(GuiGraphics graphics){for(int i=0;i<9;i++){var slot=menu.slots.get(i);if(!slot.getItem().isEmpty())graphics.renderItem(slot.getItem(),leftPos+slot.x,topPos+slot.y);}}
    private static void outline(GuiGraphics g,int x,int y,int w,int h,int c){g.fill(x,y,x+w,y+1,c);g.fill(x,y+h-1,x+w,y+h,c);g.fill(x,y,x+1,y+h,c);g.fill(x+w-1,y,x+w,y+h,c);}
}
