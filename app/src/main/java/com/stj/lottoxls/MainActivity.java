package com.stj.lottoxls;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity
{
    private EditText[] edNum; //6개의 EditText를 배열화 시켜서 반복 검사가 가능하게 선언
    private Button btnGetXLS; //엑셀 가져오기 버튼. 현재는 안씀
    private TextView tvGetXLS; //엑셀을 정상적으로 읽어왔는지 확인하는 텍스트뷰. 현재 안씀
    private Button btnReset; //초기화 버튼
    private Button btnResult; //결과 보기 버튼
    private String getFileUri; //인텐트로 파일의 주소값을 가져온 뒤 실제 파일주소로 바뀌는 주소를 저장하기 위해 선언. 현재는 안씀

    private int allCnt, subCnt, lastCnt, finishCnt; //아래 for 반복문을 돌리기 위해 사용하는 정수형 변수. 필요할 때 매번 초기화 후 가져다 쓰기때문에 중요한 역할은 아님
    private boolean checkLotto = true; //EditText가 0이아닌지, 45보다 작은지, 빈칸인지를 검사하기 위해 만든 true, false를 반환하는 변수
    private boolean winLotto = false; //입력한 숫자가 당첨인지 아닌지 검사하는 변수
    private String[][] lottoChart; //엑셀에서 읽어온 데이터들을 저장하기 위한 변수

    private ListView listViewLotto; //당첨된 회차를 뿌려주기 위한 리스트뷰
    private ListViewAdapter adapter; //리스트뷰에 들어가는 정보는 단일이 아니라 회차, 숫자여섯개 이므로 리스트뷰 아이템을 커스텀해서 보여줘야하기 때문에 adapter를 따로 제작

    //2020-03-11 추가. 로또 이미지 표시
    private ImageView[] imgLotto;
    private ImageView[] imgGetLotto;
    private int[] imgIdArray = {R.id.img_lo1, R.id.img_lo2, R.id.img_lo3, R.id.img_lo4, R.id.img_lo5, R.id.img_lo6};
    private int[] imgGetIdArray = {R.id.img_get_lo1, R.id.img_get_lo2, R.id.img_get_lo3, R.id.img_get_lo4, R.id.img_get_lo5, R.id.img_get_lo6};
    private TextView tvCount;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init(); //UI 초기 설정 함수

        //스마트폰 내부에서 파일을 가져오기 위해서는 해당 권한 요청이 필요함. Assets 폴더에서 가져오는것은 필요없기에 주석처리
        //getPermissionState();

        //현재 앱 실행 시 AsyncTask로 Assets 폴더에 있는 엑셀파일을 읽어오기 때문에
        //기존에 엑셀 읽어오기 버튼을 눌러야 작동했던 Task를 메인으로 둠
        GetXLS task = new GetXLS();
        task.execute();

        //초기화 버튼
        btnReset.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //EditText를 전부 빈칸으로 만듦. 이렇게 사용하기 위해 EditText를 배열변수로 선언
                for (allCnt = 0; allCnt < 6; allCnt++)
                    edNum[allCnt].setText("");

                //당첨된 리스트가 있어서 생성되어있다면 없애고, 새로고침해줌
                adapter.clear();
                adapter.notifyDataSetChanged();
            }
        });

        //결과값 보기 버튼
        //해당 버튼의 구조는 빈칸검사 -> 45이하, 0입력검사 -> 중복 검사 순서대로 이루어지고
        //한번이라도 조건이 안맞는게 있다면 checkLotto 변수에 false를 반환함.
        btnResult.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                for (allCnt = 0; allCnt < 6; allCnt++)
                {
                    //해당 변수로 for문이 돌 때마다 한번이라도 값이 거짓인지 검사
                    //거짓이라면 break문을 통해 상위 반복문(for)문을 나가게됨
                    if (!checkLotto)
                        break;

                    //공백이 있는 빈칸, 아얘 빈칸 모두 빈칸으로 인식
                    if (edNum[allCnt].getText().toString().replace(" ", "").equals(""))
                    {
                        checkLotto = false;
                        Toast.makeText(MainActivity.this, "모든 숫자를 입력해 주세요.", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    else
                    {
                        //EditText값은 기본적으로 String형태이므로 숫자와 비교하기 위해 int값으로 변환 후 조건검사
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
                            for (subCnt = 0; subCnt < allCnt; subCnt++)
                                if (Integer.parseInt(edNum[allCnt].getText().toString()) == Integer.parseInt(edNum[subCnt].getText().toString()))
                                {
                                    checkLotto = false;
                                    Toast.makeText(MainActivity.this, "숫자는 중복할 수 없습니다.", Toast.LENGTH_SHORT).show();
                                    break;
                                }
                    }
                }

                //한번이라도 false가 반환된적이 없다면 초기값인 true로 반환되어 EditText값과 엑셀값의 비교함수 실행
                if (checkLotto)
                {
                    CompareXLS task1 = new CompareXLS();
                    task1.execute();
                }

                //false가 반환되었다면 이렇게 초기화를 해야 해당 버튼을 재사용 할 수 있음
                checkLotto = true;
            }
        });

        /* 기존 엑셀 가져오기 버튼의 동작 코드, 필요없음
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
           */
    }

    private void init() //버튼, EditText, ListView의 기본 UI를 맞춰주는 작업
    {
        //아래 배열로 만약 edNum[3]을 호출 시 findViewById(R.id.ed_num4)를 호출하는 것과 같은 효과
        //주의 : index는 0부터 시작
        edNum = new EditText[]{findViewById(R.id.ed_num1), findViewById(R.id.ed_num2), findViewById(R.id.ed_num3),
                findViewById(R.id.ed_num4), findViewById(R.id.ed_num5), findViewById(R.id.ed_num6)};

        //가져오는 버튼및 결과텍스트. 필요없기에 주석화
        //btnGetXLS = findViewById(R.id.btn_get_xls);
        //tvGetXLS = findViewById(R.id.tv_get_xls);

        btnReset = findViewById(R.id.btn_reset);
        btnResult = findViewById(R.id.btn_result);
        edNum[0] = findViewById(R.id.ed_num1);
        edNum[1] = findViewById(R.id.ed_num2);
        edNum[2] = findViewById(R.id.ed_num3);
        edNum[3] = findViewById(R.id.ed_num4);
        edNum[4] = findViewById(R.id.ed_num5);
        edNum[5] = findViewById(R.id.ed_num6);
        listViewLotto = findViewById(R.id.lv_lotto);
        adapter = new ListViewAdapter();

        //파일 가져오기로 하였을때 권한 획득없이 실행되면 강제종료 상황 발생. 그래서 비활성화
        //btnGetXLS.setEnabled(false); //권한 획득 시 활성화로 바꿔줌

        //모두 클릭 및 포커스를 비활성화 시켜두고, 엑셀을 읽어오기가 완료되면 활성화로 바꿔줌
        //활성화 구문은 당연히 엑셀 읽어오기가 완료된 함수에 들어있음
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

    //Assets에 있는 엑셀 파일을 가져와서 읽고, lottoChart에 넣어주는 작업 함수
    //AsyncTask 는 백그라운드에서 작업을 돌릴때 손실없이 돌리기 위해 사용하는 함수
    //내부에서는 onPreExecute() -> doInBackground() -> onPostExecute() 순서로 순차적으로 작동됨
    private class GetXLS extends AsyncTask
    {
        //기존 파일 선택후 가져오기 기능에서는 필요했으나, 현재 사용하지 않음
        //FileInputStream fis = null;

        HSSFWorkbook workbook; //엑셀을 읽어옴
        HSSFSheet sheet; //시트 수
        HSSFRow row; //행 읽어오는 변수
        HSSFCell cell; //셀을 읽어오는 변수

        int rowindex; //행
        int columnindex; //열
        int rows; //행의 수
        int cells; //셀의 수
        String value = ""; //셀 값이 임시로 저장되는 변수
        int listIndex; //배열에 저장하기 위한 인덱스 변수

        //Asset 내부 파일을 읽어오기 위한 변수
        AssetManager am = getResources().getAssets();
        InputStream is = null;

        //화면에 아무런 동작을 하지 않고 백그라운드 작업이 된다면 작업중에 동작이 죽을 수도 있음
        //그래서 화면 UI작업으로 동작이 죽지 않게 해주는 로딩 다이얼로그
        ProgressDialog asyncDialog = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute()
        {
            //쓰레드가 시작될 때 로딩 다이얼로그를 띄워줌
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
            //쓰레드가 동작중에 엑셀가져오기 -> 행 및 열값 체크 -> lottoChart 배열에 넣어주기
            //위와같은 동작을 반복함
            //혹시나 모를 에러로 강제종료를 방지하기 위해 try 같은 예외 처리 구문을 사용함
            try
            {
                //기존 파일 선택후 가져오기 기능에서는 필요했으나, 현재 사용하지 않음
                //fis = new FileInputStream(getFileUri);

                is = am.open("priper.xls"); //Assets 파일은 이렇게 열어줌. 되도록 영어 파일 이름을 사용해야함. 한글일경우 유니코드 인식이 제대로 안될 가능성이있음

                workbook = new HSSFWorkbook(is); //엑셀을 열어줌
                sheet = workbook.getSheetAt(0); //첫번째 시트를 열어줌
                rows = sheet.getPhysicalNumberOfRows() - 1; //전체 행의 갯수를 체크함. 800개 행일경우 인덱스는 0인데 행은 1부터 시작되기에 1을 빼줌

                lottoChart = new String[rows][7]; //lottoChart 배열의 크기를 행은 전체 행의 갯수, 열은 7로 지정해줌

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

                            if (cell == null) //셀이 빈값일경우를 위한 널체크
                                continue;
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
                                //셀의 내용이 모두 10이면 10.0으로 나오기 때문에 .0을 빈칸으로 반환해서 해당 배열에 넣음
                                lottoChart[rowindex][columnindex] = value.replace(".0", "");
                            }
                        }
                    }

                    //전체 행을 100퍼센트로 계산해서 읽어오는 행의 갯수마다 로딩 다이얼로그를 올려줌
                    double pers = ((double) rowindex / (double) rows) * 100;
                    asyncDialog.setProgress((int) pers);

                    //작업이 너무 빠르기 때문에 못받아오는 값이 생겨서 0.0001초씩 딜레이를 줘서 전부 받아올 수 있게 함
                    Thread.sleep(1);
                }
            } catch (IOException | InterruptedException e)
            {
                e.printStackTrace();
            }
            if (is != null) //엑셀 파일을 받아오는게 끝났다면 엑셀파일을 열기 위해 사용했던 임시 Stream을 닫아줌
            {
                try
                {
                    is.close();
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o)
        {
            super.onPostExecute(o);

            //엑셀을 정상적으로 모두 읽어오게 되면, 모든 UI를 활성화 시켜줌
            btnReset.setEnabled(true);
            btnResult.setEnabled(true);
            edNum[0].setEnabled(true);
            edNum[1].setEnabled(true);
            edNum[2].setEnabled(true);
            edNum[3].setEnabled(true);
            edNum[4].setEnabled(true);
            edNum[5].setEnabled(true);

            //엑셀을 정상적으로 읽어왔다고 표기해줌. 현재 안씀
            //tvGetXLS.setText("엑셀을 읽어 왔습니다.");

            //로딩 다이얼로그 종료
            asyncDialog.dismiss();
        }
    }

    //엑셀 데이터가 저장된 lottoChart배열과 6개의 EditText를 비교하여 결과를 뽑아내는 함수
    //AsyncTask 는 백그라운드에서 작업을 돌릴때 손실없이 돌리기 위해 사용하는 함수
    //내부에서는 onPreExecute() -> doInBackground() -> onPostExecute() 순서로 순차적으로 작동됨
    private class CompareXLS extends AsyncTask
    {
        String[] check; //당첨된 회차를 임시로 저장하는 변수
        String[][] result = new String[lottoChart.length][7]; //당첨 회차가 여러개 일 수도 있으니, 2차원 배열로 check배열에서 임시저장된 변수를 그때마다 행을 나눠서 저장
        int cnt = 0; //result의 행을 바꾸기 위한 임시변수

        //마찬가지로 손실 에러 방지를 위한 UI 로딩 다이얼로그
        ProgressDialog asyncDialog = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute()
        {
            //퍼센티지가 아닌 원형 다이얼로그
            //이유는 굳이 100퍼센트로 나눌 데이터가 없고, 적은 데이터값을 뽑아내기 때문
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
            //당첨된 회차, 숫자 여섯개가 임시로 담길 check변수의 크기를 7로 설정
            check = new String[7];

            for (allCnt = 0; allCnt < lottoChart.length; allCnt++)
            {
                for (subCnt = 1; subCnt < lottoChart[allCnt].length; subCnt++)
                    for (lastCnt = 0; lastCnt < edNum.length; lastCnt++)
                        if (edNum[lastCnt].getText().toString().equals(lottoChart[allCnt][subCnt]))
                            //당첨된 회차의 숫자 6개만 check배열에 임시저장
                            //이유는 입력된 숫자와 회차가 비교될 시 오류를 뱉음
                            check[subCnt] = lottoChart[allCnt][subCnt];

                //임시 저장된 숫자6개가 값이 null이 아닐경우에만 최종 결과로 저장
                if (check[1] != null && check[2] != null && check[3] != null && check[4] != null && check[5] != null && check[6] != null)
                {
                    //당첨된 숫자의 회차정보는 여기서 넣어줌
                    check[0] = lottoChart[allCnt][0];

                    //result[행][열], 행은 위에 for문이 반복되서 당첨회차를 찾을 경우에 계속 cnt가 늘어나니 최종 저장이 가능
                    for (finishCnt = 0; finishCnt < 7; finishCnt++)
                        result[cnt][finishCnt] = check[finishCnt];

                    check = new String[7]; //임시변수를 재생성해줌 이유는 데이터가 있는 상태에서는 또다른 데이터를 받을시 에러

                    // 당첨회차가 여러개라면 반복해서 true로 계속 주겠지만 상관없음. 의도된 개발
                    winLotto = true; //당첨되었다고 변수 변환. 초기값이 맨 위에서 false였으니 당첨이 되지 않으면 자동 false.

                    cnt++; //result 배열의 행 역할을 할 변수를 1씩 더해줌
                }
                else
                    check = new String[7]; //임시변수를 재생성해줌 이유는 데이터가 있는 상태에서는 또다른 데이터를 받을시 에러
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o)
        {
            super.onPostExecute(o);

            //당첨 된 회차가 있을 때. winLotto=true
            if (winLotto)
            {
                //기존 리스트 모두 삭제 후 새로고침
                adapter.clear();
                adapter.notifyDataSetChanged();

                //알림을 띄워서 시각적으로 확인 가능
                Toast.makeText(MainActivity.this, "당첨 되었습니다.", Toast.LENGTH_SHORT).show();

                //저장된 당첨회차가 여러개일 것을 대비해서 행만 카운트로 바꿔줌. 해당 행은 0부터 cnt크기까지. 이유는 cnt는 초기화가 안되서 내려오기 때문에 최종 당첨 회차들의 갯수와도 같음
                //그리고 cnt 변수는 해당 AsyncTask의 지역변수로 초기에 0으로 초기화되기 때문에 굳이 초기화 작업이 필요없음.
                for (allCnt = 0; allCnt < cnt; allCnt++)
                    adapter.addItem(result[allCnt][0], result[allCnt][1], result[allCnt][2], result[allCnt][3], result[allCnt][4], result[allCnt][5], result[allCnt][6]);

                //2020-03-11 추가
                final AlertDialog.Builder dialogLotto = new AlertDialog.Builder(MainActivity.this);

                LayoutInflater inflater = getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.dialog_lottoball, null);

                int[] imgs = new int[46]; //로또 이미지 배열저장
                imgLotto = new ImageView[6]; //당첨회차 배열 저장
                imgGetLotto = new ImageView[6]; //작성번호 배열 저장

                tvCount = dialogView.findViewById(R.id.tv_count);

                for (int i = 1; i <= 45; i++)
                {
                    imgs[i] = getApplicationContext().getResources().getIdentifier("lottoball" + i, "drawable", "com.stj.lottoxls");
                    //배열로 로또이미지를 int 형태로 저장
                }

                for (int i = 0; i < 6; i++)
                {
                    imgLotto[i] = dialogView.findViewById(imgIdArray[i]); //변수를 id값으로 선언
                    imgGetLotto[i] = dialogView.findViewById(imgGetIdArray[i]); //변수를 id값으로 선언
                }

                for (int i = 0; i < 6; i++)
                {
                    imgLotto[i].setImageResource(imgs[Integer.parseInt(result[0][i + 1])]); //당첨 회차를 불러와서 해당 String을 int로 변환 후 배열값에 넣고 로또이미지 불러오기
                    imgGetLotto[i].setImageResource(imgs[Integer.parseInt(edNum[i].getText().toString())]); //작성 회차를 불러와서 해당 String을 int로 변환 후 배열값에 넣고 로또이미지 불러오기
                }

                tvCount.setText(result[0][0]); //회차 정보 표기

                dialogLotto.setPositiveButton("확인", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                });
                dialogLotto.setView(dialogView);
                dialogLotto.setCancelable(false);
                dialogLotto.create();
                dialogLotto.show();

                //리스트뷰 새로고침
                adapter.notifyDataSetChanged();

                //당첨 확인 변수 초기화
                winLotto = false;
            }

            //당첨 회차가 없을 때. winLotto=false
            else
            {
                //알림 띄워줌
                Toast.makeText(MainActivity.this, "당첨 목록이 없습니다.", Toast.LENGTH_SHORT).show();

                //리스트뷰가 혹시나 남아있어서 헷갈릴 경우를 대비해 전부 삭제 및 새로고침
                adapter.clear();
                adapter.notifyDataSetChanged();
            }

            //로딩 다이얼로그 삭제
            asyncDialog.dismiss();
        }
    }

    /*
    //파일 가져오기 시 필요한 권한 획득 함수. 현재 필요없음
    private void getPermissionState()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
        {
            //권한 비활성화 시 권한 요청 메서드
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }

        else
        {
            //권한 활성화 시
            btnGetXLS.setEnabled(true);
        }
    }

    //현재 방식인 Assets에서 엑셀파일을 읽어오는 함수지만 밑에 엑셀 읽어오는 AsyncTask 함수에 통일시켜서 현재 필요없음
    public InputStream getAssetXLS()
    {
        AssetManager am = getResources().getAssets();
        InputStream is = null;

        try
        {
            is = am.open("priper.xls");

            // TODO : use is(InputStream).

        } catch (Exception e)
        {
            e.printStackTrace();
        }

        if (is != null)
        {
            try
            {
                is.close();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return is;
    }

    //스마트폰에서 파일관리자로 가서 파일을 선택하고 다시 액티비티로 돌아왔을때 변화를 인지하고 엑셀파일을 읽어주기 시작하는 구문
    //현재 필요없음
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

    //파일 자체를 어플로 가져와서 집어넣는게 아닌 파일의 실제 경로를 따서 읽어와야 하기 때문에 필요한 구문
    //현재 필요없음
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

    //getFilePath함수에서 파일 유형에 따라 사용하기 위해 붙는 함수
    public static boolean isExternalStorageDocument(Uri uri)
    {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    //getFilePath함수에서 파일 유형에 따라 사용하기 위해 붙는 함수
    public static boolean isDownloadsDocument(Uri uri)
    {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    //getFilePath함수에서 파일 유형에 따라 사용하기 위해 붙는 함수
    public static boolean isMediaDocument(Uri uri)
    {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    //getFilePath함수에서 파일 유형에 따라 사용하기 위해 붙는 함수
    public static boolean isGooglePhotosUri(Uri uri)
    {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    //getFilePath함수에서 파일 유형에 따라 사용하기 위해 붙는 함수
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

    //안드로이드 Oreo 버전부터는 getFilePath 함수로만 경로를 읽어올 수 없고 캐시파일을 따로 만들어줘야 읽어지는데 그 부분을 위한 함수
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
    */
}

