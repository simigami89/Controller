package com.example.controller;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigInteger;
import java.util.ArrayList;

import biz.kasual.materialnumberpicker.MaterialNumberPicker;

/**
 * Created by Екатерина Захарова on 26.02.2016.
 */
public class ConfigAdapter extends BaseExpandableListAdapter {
    final String DEBUG = "DEBUG";
    private Context mContext;
    private ArrayList<ArrayList<String>> mGroups;
    MaterialNumberPicker numberPicker;

    public ConfigAdapter(Context context, ArrayList<ArrayList<String>> groups) {
        this.mContext = context;
        this.mGroups = groups;
    }

    public int getGroupCount() {
        return this.mGroups.size();
    }

    public int getChildrenCount(int groupPosition) {
        return ((ArrayList) this.mGroups.get(groupPosition)).size();
    }

    public Object getGroup(int groupPosition) {
        return this.mGroups.get(groupPosition);
    }

    public Object getChild(int groupPosition, int childPosition) {
        return ((ArrayList) this.mGroups.get(groupPosition)).get(childPosition);
    }

    public long getGroupId(int groupPosition) {
        return (long) groupPosition;
    }

    public long getChildId(int groupPosition, int childPosition) {
        return (long) childPosition;
    }

    public boolean hasStableIds() {
        return true;
    }

    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.group_view, null);
        }
        if (isExpanded) {
            ((ImageView) convertView.findViewById(R.id.indicator)).setImageResource(R.drawable.ic_expand_less_black_24dp);
        } else {
            ((ImageView) convertView.findViewById(R.id.indicator)).setImageResource(R.drawable.ic_expand_more_black_24dp);
        }
        TextView textGroup = (TextView) convertView.findViewById(R.id.textGroup);
        if (groupPosition == 0) {
            textGroup.setText("Settings");
        } else if (groupPosition == 1) {
            textGroup.setText("Accidents");
        } else if (groupPosition == 2) {
            textGroup.setText("Checking flag");
        } else if (groupPosition == 3) {
            textGroup.setText("Linking");
        }
        return convertView;
    }

    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (groupPosition == 0) {
            final Ustavki ustavki = Ustavki.getInstance();
            convertView = inflater.inflate(R.layout.config_ustavki, null);
            ((TextView) convertView.findViewById(R.id.ustavka_name)).setText((CharSequence) ((ArrayList) this.mGroups.get(groupPosition)).get(childPosition));
            final TextView textUstavkaValue = (TextView) convertView.findViewById(R.id.ustavka_value);
            if (childPosition == 0) {
                textUstavkaValue.setText(ustavki.getIozz());
            } else if (childPosition == 1) {
                textUstavkaValue.setText(ustavki.getImfz());
            }
            final View finalConvertView = convertView;
            final int i = childPosition;
            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    Log.d("DEBUG", "LONG--PUSH");
                    numberPicker = new MaterialNumberPicker.Builder(finalConvertView.getContext()).minValue(1).maxValue(10).defaultValue(5).backgroundColor(-1).separatorColor(0).textColor(-16777216).textSize(20.0f).enableFocusability(false).wrapSelectorWheel(true).build();
                    new AlertDialog.Builder(finalConvertView.getContext()).setTitle("Set new value").setView(numberPicker).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String str = String.valueOf(numberPicker.getValue());
                            textUstavkaValue.setText(str);
                            if (i == 0) {
                                ustavki.setIozz(str);
                            } else if (i == 1) {
                                ustavki.setImfz(str);
                            }
                            Toast.makeText(finalConvertView.getContext(), str, Toast.LENGTH_SHORT).show();
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
                    return true;
                }
            });
        } else if (groupPosition == 3) {
            convertView = inflater.inflate(R.layout.config_linking_child, null);
            final View finalConvertView = convertView;
            final DeviceInfo deviceInfo = DeviceInfo.getInstance();
            final TextView textChildValue = (TextView) convertView.findViewById(R.id.linking_child2);
            ((TextView) convertView.findViewById(R.id.linking_child1)).setText((CharSequence) ((ArrayList) this.mGroups.get(groupPosition)).get(childPosition));
            String str = null;
            if (childPosition == 0) {
                str = deviceInfo.getDevice1();
            } else if (childPosition == 1) {
                str = deviceInfo.getDevice2();
            } else if (childPosition == 2) {
                str = deviceInfo.getPhase();
            }
            textChildValue.setText(str);
            InputFilter[] FilterArray = new InputFilter[]{new InputFilter.LengthFilter(12)};
            final EditText editText = new EditText(convertView.getContext());
            editText.setFilters(FilterArray);
            editText.setFocusable(true);
            editText.setMaxLines(1);
            editText.setText(str);
            //finalConvertView1 = convertView;
            final int i2 = childPosition;
            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    Log.d("DEBUG", "LONG--PUSH");
                    new AlertDialog.Builder(finalConvertView.getContext()).setTitle("Set new value").setView(editText).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String str = String.valueOf(editText.getText());

                            if (i2 == 0) {
                                byte[] bytes = hexStringToByteArray(str);
                                deviceInfo.setDevice1(str);
                                ConnectedActivity.writeCharacteristicValue(ConnectedActivity.SERVICE_BANK_INFO, ConnectedActivity.DEVICE_1_ADDRESS, bytes, finalConvertView);
                            } else if (i2 == 1) {
                                byte[] bytes = hexStringToByteArray(str);
                                deviceInfo.setDevice2(str);
                                ConnectedActivity.writeCharacteristicValue(ConnectedActivity.SERVICE_BANK_INFO, ConnectedActivity.DEVICE_2_ADDRESS, bytes, finalConvertView);
                            } else if (i2 == 2) {
                                deviceInfo.setPhase(str);
                                String ss = toHex(str);
                                Log.d(DEBUG,ss);
                                byte[] bytes = hexStringToByteArray(ss);
                                ConnectedActivity.writeCharacteristicValue(ConnectedActivity.SERVICE_BANK_INFO, ConnectedActivity.PHASE, bytes, finalConvertView);
                            }
                            textChildValue.setText(str);
                            Toast.makeText(finalConvertView.getContext(), str, Toast.LENGTH_SHORT).show();
                            ((ViewManager) editText.getParent()).removeView(editText);
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
                    return true;
                }
            });
        } else {
            convertView = inflater.inflate(R.layout.config_common_child, null);
            final View finalConvertView = convertView;
                    ((TextView) convertView.findViewById(R.id.textChild)).setText((CharSequence) ((ArrayList) this.mGroups.get(groupPosition)).get(childPosition));
            if (groupPosition == 1 && childPosition == 1) {
//                finalConvertView = convertView;
                convertView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        byte[] tx = new byte[1];
                        tx[0] = (byte) 49;
                        ConnectedActivity.writeCharacteristicValue(ConnectedActivity.LED_SERVICE, ConnectedActivity.CHAR_LED_BLINK, tx, finalConvertView);
                    }
                });
            }
        }
        return convertView;
    }

    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[(len / 2)];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public String toHex(String arg) {
        return String.format("%x", new Object[]{new BigInteger(1, arg.getBytes())});
    }
}
