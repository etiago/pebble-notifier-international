#include <pebble.h>

static Window *window;

static BitmapLayer *character_layer;
static GBitmap character_bitmap;
// 64 is number of bytes (uint8_t)
// each chinese character = 32 bytes (2 wide by 16)
// each western character = 16 bytes (1 wide by 16)
// 288 bytes is 144 pixels across, divided by 8 to get bytes times 16 rows
#define BITMAP_WIDTH 20
#define BITMAP_HEIGHT 160
static uint8_t character_data[BITMAP_WIDTH * BITMAP_HEIGHT];

static int offset = 0;

void in_received_handler(DictionaryIterator *received, void *context) {
   // incoming message received
    app_comm_set_sniff_interval(SNIFF_INTERVAL_REDUCED);
	app_comm_set_sniff_interval(SNIFF_INTERVAL_NORMAL);

	Tuple* message = dict_find(received, 1);

	Tuple* reset = dict_find(received, 2);

	Tuple* finished = dict_find(received, 3);
	APP_LOG(APP_LOG_LEVEL_DEBUG, "Received a message!");
	if (reset != NULL) {
		offset = 0;
		memset(character_data, 255, 3200);
		APP_LOG(APP_LOG_LEVEL_DEBUG, "It's a message to reset the message!");
	}

	if (finished != NULL) {
		layer_mark_dirty(bitmap_layer_get_layer(character_layer));	
		vibes_short_pulse();
		APP_LOG(APP_LOG_LEVEL_DEBUG, "It's a message to finish the message!");
		return;
	}

	//size_t offset = i * 2;

	if (message != NULL) {
		memcpy(&character_data[offset], message->value->data, message->length);
		offset+=BITMAP_WIDTH;
		
	}

	/*Tuplet value = TupletInteger(1, message->value->data[0]);
  
	DictionaryIterator *iter;
	app_message_out_get(&iter);
	
	if (iter == NULL) {
		return;
	}
	
	dict_write_tuplet(iter, &value);
	dict_write_end(iter);
	
	app_message_out_send();
	app_message_out_release();*/
	//memcpy(character_data + offset, message->value->data + 1, message->length - 1);
   //text_layer_set_text(&hello_layer, message->value->cstring);
 }
 
 void in_dropped_handler(AppMessageResult reason, void *context) {
   // incoming message dropped
   /*if (reason == APP_MSG_CALLBACK_NOT_REGISTERED) {
		text_layer_set_text(&hello_layer, "Hello not registered!");
	} else if (reason == APP_MSG_CALLBACK_ALREADY_REGISTERED) {
		text_layer_set_text(&hello_layer, "Hello already registered!");
	} else if (reason == APP_MSG_ALREADY_RELEASED) {
		text_layer_set_text(&hello_layer, "Hello already released!");
	}else if (reason == APP_MSG_BUFFER_OVERFLOW) {
		text_layer_set_text(&hello_layer, "Hello buff over!");
	}else if (reason == APP_MSG_BUSY) {
		text_layer_set_text(&hello_layer, "Hello busy!");
	}else if (reason == APP_MSG_INVALID_ARGS) {
		text_layer_set_text(&hello_layer, "Hello invalid args!");
	}else if (reason == APP_MSG_APP_NOT_RUNNING) {
		text_layer_set_text(&hello_layer, "Hello not running!");
	}else if (reason == APP_MSG_OK) {
		text_layer_set_text(&hello_layer, "Hello ok!");
	}else if (reason == APP_MSG_NOT_CONNECTED) {
		text_layer_set_text(&hello_layer, "Hello not connected!");
	}else if (reason == APP_MSG_SEND_REJECTED) {
		text_layer_set_text(&hello_layer, "Hello send rejected!");
	}else if (reason == APP_MSG_SEND_TIMEOUT) {
		text_layer_set_text(&hello_layer, "Hello send timeout!");
	}else {
		text_layer_set_text(&hello_layer, "Hello Dropped!");
	}*/
 }
 
 static void window_load(Window *window) {
        // Load layers for the window and add them
    memset(character_data, 255, 3200);
	APP_LOG(APP_LOG_LEVEL_DEBUG, "bitmap now is null: %p", &character_bitmap);
	// Album art
    character_bitmap = (GBitmap) {
        .addr = character_data,
        .bounds = GRect(0, 0, 144, 160),
        .info_flags = 1,
        .row_size_bytes = 20,
    };
    
    APP_LOG(APP_LOG_LEVEL_DEBUG, "Get new bitmap: %p", character_data);
    
    //memset(album_art_data, 0, 512);
    character_layer= bitmap_layer_create(GRect(0, 0, 144, 160));
	bitmap_layer_set_background_color(character_layer, GColorWhite);
    bitmap_layer_set_bitmap(character_layer, &character_bitmap);
    layer_add_child(window_get_root_layer(window), bitmap_layer_get_layer(character_layer));
        
    }
    
static void window_unload(Window *window) {

}

static void init(void) {
  window = window_create();
  app_message_register_inbox_received(in_received_handler);
  app_message_register_inbox_dropped(in_dropped_handler);
  const uint32_t inbound_size = 256;
  const uint32_t outbound_size = 32;
  app_message_open(inbound_size, outbound_size);
  window_set_window_handlers(window, (WindowHandlers) {
        .load = window_load,
        
        .unload = window_unload
    });
   
  const bool animated = true;
  window_stack_push(window, animated); 
}

static void deinit(void) {
  bitmap_layer_destroy(character_layer);
  window_destroy(window);
}

int main(void) {
  init();

  APP_LOG(APP_LOG_LEVEL_DEBUG, "Done initializing, pushed window: %p", window);

  app_event_loop();
  deinit();
}
