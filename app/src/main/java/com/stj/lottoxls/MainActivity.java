package com.stj.lottoxls;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity
{
    private EditText[] edNum;
    private Button btnGetXLS, btnReset, btnResult;
    private TextView tvGetXLS;
    private String getFileUri;

    private int allCnt, subCnt, lastCnt, finishCnt;
    private boolean checkLotto = true, winLotto = false;
    private String[][] lottoChart;

    private ListView listViewLotto;
    private ListViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        getPermissionState();

        btnGetXLS.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent_upload = new Intent();
                intent_upload.setType("application/vnd.ms-excel");
                intent_upload.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent_upload, 1);
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                for (allCnt = 0; allCnt < 6; allCnt++)
                {
                    edNum[allCnt].setText("");
                }

                adapter.clear();
                adapter.notifyDataSetChanged();
            }
        });

        btnResult.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                for (allCnt = 0; allCnt < 6; allCnt++)
                {
                    if (!checkLotto)
                        break;

                    if (edNum[allCnt].getText().toString().replace(" ", "").equals(""))
                    {
                        checkLotto = false;
                        Toast.makeText(MainActivity.this, "모든 숫자를 입력해 주세요.", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    else
                    {
                        if (Integer.parseInt(edNum[allCnt].getText().toString()) > 45)
                        {
                            checkLotto = false;
                            Toast.makeText(MainActivity.this, "숫자는 45를 넘을 수 없습니다.", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        else if (Integer.parseInt(edNum[allCnt].getText().toString()) <= 0)
                        {
                            checkLotto = false;
                            Toast.makeText(MainActivity.this, "0은 입력할 수 없습니다.", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        else
                        {
                            for (subCnt = 0; subCnt < allCnt; subCnt++)
                            {
                                if (Integer.parseInt(edNum[allCnt].getText().toString()) == Integer.parseInt(edNum[subCnt].getText().toString()))
                                {
                                    checkLotto = false;
                                    Toast.makeText(MainActivity.this, "숫자는 중복할 수 없습니다.", Toast.LENGTH_SHORT).show();
                                    break;
                                }
                            }
                        }
                    }
                }

                if (checkLotto)
                {
                    CompareXLS task1 = new CompareXLS();
                    task1.execute();
                }

                checkLotto = true;
            }
        });
    }

    private void init()
    {
        edNum = new EditText[]{findViewById(R.id.ed_num1), findViewById(R.id.ed_num2), findViewById(R.id.ed_num3),
                findViewById(R.id.ed_num4), findViewById(R.id.ed_num5), findViewById(R.id.ed_num6)};

        btnGetXLS = findViewById(R.id.btn_get_xls);
        btnReset = findViewById(R.id.btn_reset);
        btnResult = findViewById(R.id.btn_result);
        tvGetXLS = findViewById(R.id.tv_get_xls);
        edNum[0] = findViewById(R.id.ed_num1);
        edNum[1] = findViewById(R.id.ed_num2);
        edNum[2] = findViewById(R.id.ed_num3);
        edNum[3] = findViewById(R.id.ed_num4);
        edNum[4] = findViewById(R.id.ed_num5);
        edNum[5] = findViewById(R.id.ed_num6);
        listViewLotto = findViewById(R.id.lv_lotto);
        adapter = new ListViewAdapter();

        btnGetXLS.setEnabled(false); //권한 획득 시 활성화

        //파일 가져오기 완료 시 활성화
        btnReset.setEnabled(false);
        btnResult.setEnabled(false);
        edNum[0].setEnabled(false);
        edNum[1].setEnabled(false);
        edNum[2].setEnabled(false);
        edNum[3].setEnabled(false);
        edNum[4].setEnabled(false);
        edNum[5].setEnabled(false);

        listViewLotto.setAdapter(adapter);
    }

    private void getPermissionState()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }

        else
        {
            btnGetXLS.setEnabled(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null)
        {
            Uri fileUri = data.getData();

            try
            {
                getFileUri = getFilePath(MainActivity.this, fileUri);
                tvGetXLS.setText(getFileUri);

                GetXLS task = new GetXLS();
                task.execute();
            } catch (URISyntaxException e)
            {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("NewApi")
    public static String getFilePath(Context context, Uri uri) throws URISyntaxException
    {
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        if (Build.VERSION.SDK_INT >= 19 && DocumentsContract.isDocumentUri(context.getApplicationContext(), uri))
        {
            if (isExternalStorageDocument(uri))
            {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            }

            else if (isDownloadsDocument(uri))
            {
                final String id = DocumentsContract.getDocumentId(uri);

                if (id.startsWith("raw:"))
                {
                    return id.replaceFirst("raw:", "");
                }
                long fileId = getFileId(uri);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                {
                    try
                    {
                        InputStream inputStream = context.getContentResolver().openInputStream(uri);
                        File file = new File(context.getCacheDir().getAbsolutePath() + "/" + fileId);
                        writeFile(inputStream, file);
                        return file.getAbsolutePath();
                    } catch (FileNotFoundException e)
                    {
                        e.printStackTrace();
                    }
                }
                else
                {
                    String[] contentUriPrefixesToTry = new String[]{
                            "content://downloads/public_downloads",
                            "content://downloads/my_downloads",
                            "content://downloads/all_downloads"
                    };

                    for (String contentUriPrefix : contentUriPrefixesToTry)
                    {
                        try
                        {
                            Uri contentUri = ContentUris.withAppendedId(Uri.parse(contentUriPrefix), fileId);

                        } catch (Exception e)
                        {
                            Log.d("content-Provider", e.getMessage());
                        }
                    }
                }
            }
            else if (isMediaDocument(uri))
            {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type))
                {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                }
                else if ("video".equals(type))
                {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                }
                else if ("audio".equals(type))
                {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{
                        split[1]
                };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme()))
        {
            if (isGooglePhotosUri(uri))
            {
                return uri.getLastPathSegment();
            }

            String[] projection = {
                    MediaStore.Images.Media.DATA
            };
            Cursor cursor = null;
            try
            {
                cursor = context.getContentResolver()
                        .query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst())
                {
                    return cursor.getString(column_index);
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else if ("file".equalsIgnoreCase(uri.getScheme()))
        {
            return uri.getPath();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri)
    {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri)
    {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri)
    {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri)
    {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    private static Long getFileId(Uri uri)
    {
        long strReulst = 0;
        try
        {
            String path = uri.getPath();
            String[] paths = path.split("/");
            if (paths.length >= 3)
            {
                strReulst = Long.parseLong(paths[2]);
            }
        } catch (NumberFormatException | IndexOutOfBoundsException e)
        {
            strReulst = Long.parseLong(new File(uri.getPath()).getName());
        }
        return strReulst;
    }

    private static void writeFile(InputStream in, File file)
    {
        OutputStream out = null;
        try
        {
            out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0)
            {
                out.write(buf, 0, len);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                if (out != null)
                {
                    out.close();
                }
                in.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private class GetXLS extends AsyncTask
    {
        FileInputStream fis = null;
        HSSFWorkbook workbook;
        HSSFSheet sheet; //시트 수
        HSSFRow row; //행 읽어오는 변수
        HSSFCell cell; //셀을 읽어오는 변수

        int rowindex; //행
        int columnindex; //열
        int rows; //행의 수
        int cells; //셀의 수
        String value = ""; //셀 값이 임시로 저장되는 변수
        int listIndex; //배열에 저장하기 위한 인덱스 변수

        ProgressDialog asyncDialog = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute()
        {
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            asyncDialog.setMessage("파일을 읽어오는 중...");
            asyncDialog.setCancelable(false);
            asyncDialog.setMax(100);
            asyncDialog.setProgress(0);
            asyncDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Object doInBackground(Object[] objects)
        {
            try
            {
                fis = new FileInputStream(getFileUri);
                workbook = new HSSFWorkbook(fis);
                sheet = workbook.getSheetAt(0);
                rows = sheet.getPhysicalNumberOfRows() - 1;

                lottoChart = new String[rows][7];

                for (rowindex = 0; rowindex < rows; rowindex++)
                {
                    row = sheet.getRow(rowindex + 1);

                    if (row != null)
                    {
                        cells = row.getPhysicalNumberOfCells();

                        for (columnindex = 0; columnindex <= cells; columnindex++)
                        {
                            //셀값을 읽는다
                            cell = row.getCell(columnindex);
                            //셀이 빈값일경우를 위한 널체크
                            if (cell == null)
                            {
                                continue;
                            }
                            else
                            {
                                //타입별로 내용 읽기
                                switch (cell.getCellType())
                                {
                                    case HSSFCell.CELL_TYPE_FORMULA:
                                        value = cell.getCellFormula();
                                        break;
                                    case HSSFCell.CELL_TYPE_NUMERIC:
                                        value = cell.getNumericCellValue() + "";
                                        break;
                                    case HSSFCell.CELL_TYPE_STRING:
                                        value = cell.getStringCellValue() + "";
                                        break;
                                    case HSSFCell.CELL_TYPE_BLANK:
                                        value = cell.getBooleanCellValue() + "";
                                        break;
                                    case HSSFCell.CELL_TYPE_ERROR:
                                        value = cell.getErrorCellValue() + "";
                                        break;
                                }
                                lottoChart[rowindex][columnindex] = value.replace(".0", "");
                            }
                        }
                    }

                    double pers = ((double) rowindex / (double) rows) * 100;
                    asyncDialog.setProgress((int) pers);

                    Thread.sleep(1);
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o)
        {
            super.onPostExecute(o);

            for (listIndex = 0; listIndex < lottoChart.length; listIndex++)
            {
                try
                {
                    Thread.sleep(1);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }

            btnReset.setEnabled(true);
            btnResult.setEnabled(true);
            edNum[0].setEnabled(true);
            edNum[1].setEnabled(true);
            edNum[2].setEnabled(true);
            edNum[3].setEnabled(true);
            edNum[4].setEnabled(true);
            edNum[5].setEnabled(true);

            asyncDialog.dismiss();
        }
    }

    private class CompareXLS extends AsyncTask
    {
        ProgressDialog asyncDialog = new ProgressDialog(MainActivity.this);

        String[] check;
        String[][] result = new String[lottoChart.length][7];
        int cnt = 0;

        @Override
        protected void onPreExecute()
        {
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage("값을 비교하는 중...");
            asyncDialog.setCancelable(false);
            asyncDialog.show();
            super.onPreExecute();
        }

        @SuppressLint("WrongThread")
        @Override
        protected Object doInBackground(Object[] objects)
        {
            check = new String[7];

            for (allCnt = 0; allCnt < lottoChart.length; allCnt++)
            {
                for (subCnt = 1; subCnt < lottoChart[allCnt].length; subCnt++)
                {
                    for (lastCnt = 0; lastCnt < edNum.length; lastCnt++)
                    {
                        if (edNum[lastCnt].getText().toString().equals(lottoChart[allCnt][subCnt]))
                        {
                            check[subCnt] = lottoChart[allCnt][subCnt];
                        }
                    }
                }

                if (check[1] != null && check[2] != null && check[3] != null && check[4] != null && check[5] != null && check[6] != null)
                {
                    check[0] = lottoChart[allCnt][0];
                    for (finishCnt = 0; finishCnt < 7; finishCnt++)
                    {
                        result[cnt][finishCnt] = check[finishCnt];
                    }

                    check = new String[7];
                    winLotto = true;
                    cnt++;
                }
                else
                {
                    check = new String[7];
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o)
        {
            super.onPostExecute(o);

            if (winLotto)
            {
                adapter.clear();
                adapter.notifyDataSetChanged();

                Toast.makeText(MainActivity.this, "당첨 되었습니다.", Toast.LENGTH_SHORT).show();
                for (allCnt = 0; allCnt < cnt; allCnt++)
                {
                    adapter.addItem(result[allCnt][0], result[allCnt][1], result[allCnt][2], result[allCnt][3], result[allCnt][4], result[allCnt][5], result[allCnt][6]);
                }
                adapter.notifyDataSetChanged();
                winLotto = false;
            }

            else
            {
                Toast.makeText(MainActivity.this, "당첨 목록이 없습니다.", Toast.LENGTH_SHORT).show();
                adapter.clear();
                adapter.notifyDataSetChanged();
            }
            asyncDialog.dismiss();
        }
    }
}

